use strict;
use warnings;

use common::sense;

use DBI;
use String::CamelCase qw(camelize);

use Carp;
use Getopt::Long;
use Pod::Usage;
use Config::Any;

use Log::Log4perl qw(:easy);
Log::Log4perl->init(\ <<'EOT');
  log4perl.category = DEBUG, Screen
  log4perl.appender.Screen = \
      Log::Log4perl::Appender::ScreenColoredLevels
  log4perl.appender.Screen.layout = \
      Log::Log4perl::Layout::PatternLayout
  log4perl.appender.Screen.layout.ConversionPattern = \
      %p %F{1} %L> %m %n
EOT

my $logger = get_logger();

my $help = 0;
my $file;
my $study;
my $config = 'missing';
my $sheet = "Sheet1";
my $database;
my $user;
my $password;

GetOptions(
  'help|?' => \$help,
  'config=s' => \$config,
) or die("Error in command line arguments\n");

if (! -e $config) {
  $logger->error("Can't find --config argument: $config");
  exit(1);
}

$logger->info("Reading config file: $config");
my $cfg = Config::Any->load_files({files => [$config], use_ext => 1});
$cfg = $cfg->[0];
my ($filename) = keys %$cfg;
$cfg = $cfg->{$filename};

if (! $cfg->{create}) {
  $logger->error("Sorry; this is not a study building script: $config");
  exit(1);
}

pod2usage(1) if $help;

my $context = {};

sub lowercaseify {
  my ($hash) = @_;
  my $result = {};
  while(my ($k, $v) = each %$hash) {
    $result->{lc($k)} = $v;
  }
  return $result;
}

sub camelHeader {
  my ($string) = @_;
  $string =~ s{\s+}{ }g;
  $string =~ s{\b(?:the|of|in|to|by|and|for)\b}{}ig;
  $string =~ s{\([^\)]+\)}{}g;
  $string =~ s{\s+$}{};
  $string =~ s{ +}{_}g;
  $string =~ s{\#}{Num}g;
  $string =~ s{[<>/\_:-]}{}g;
  $string =~ s{\?}{}g;
  $string =~ s{\&gt;}{}g;
  return camelize($string);
}

sub write_data {
  my ($context, $cfg) = @_;

  $logger->info("Writing data");

  my $study = $cfg->{study};
  my $database = $cfg->{database};
  my $username = $cfg->{username};
  my $password = $cfg->{password};
  my $commands = $cfg->{commands} // [];

  $logger->info("Connecting to the database");
  my $dbh = DBI->connect($database, $username, $password, {RaiseError => 1});
  $dbh->begin_work();
  for my $command (@$commands) {
    $dbh->do($command);
  }

  ## First find the study
  my $study_ref = $dbh->selectrow_hashref(qq{SELECT * FROM "STUDIES" WHERE "NAME" = ?}, {}, $study);

  if ($study_ref) {
    $study_ref = lowercaseify($study_ref);
    $logger->info("Found existing study: ", $study);

    $logger->info("Deleting view attributes");
    $dbh->do(qq{DELETE FROM "VIEW_ATTRIBUTES" WHERE "VIEW_ID" IN (SELECT "ID" FROM "VIEWS" WHERE "STUDY_ID" = ?)}, {}, $study_ref->{id});

    $logger->info("Deleting views");
    $dbh->do(qq{DELETE FROM "VIEWS" WHERE study_id = ?}, {}, $study_ref->{id});

    $logger->info("Deleting case values");
    $dbh->do(qq{DELETE FROM "CASE_ATTRIBUTE_STRINGS" WHERE "CASE_ID" IN (SELECT "ID" FROM "CASES" WHERE "STUDY_ID" = ?)}, {}, $study_ref->{id});
    $dbh->do(qq{DELETE FROM "CASE_ATTRIBUTE_DATES" WHERE "CASE_ID" IN (SELECT "ID" FROM "CASES" WHERE "STUDY_ID" = ?)}, {}, $study_ref->{id});
    $dbh->do(qq{DELETE FROM "CASE_ATTRIBUTE_BOOLEANS" WHERE "CASE_ID" IN (SELECT "ID" FROM "CASES" WHERE "STUDY_ID" = ?)}, {}, $study_ref->{id});

    $logger->info("Deleting cases");
    $dbh->do(qq{DELETE FROM "CASES" WHERE "STUDY_ID" = ?}, {}, $study_ref->{id});

    $logger->info("Deleting attributes");
    $dbh->do(qq{DELETE FROM "ATTRIBUTES" WHERE "STUDY_ID" = ?}, {}, $study_ref->{id});
  }

  if (! $study_ref) {
    $dbh->do(qq{INSERT INTO "STUDIES" ("NAME") VALUES (?)}, {}, $study);
    $study_ref = $dbh->selectrow_hashref(qq{SELECT * FROM "STUDIES" WHERE "NAME" = ?}, {}, $study);
    $study_ref = lowercaseify($study_ref);
    $logger->info("Created new study: ", $study);
  }

  $logger->info("Writing attributes for: ", $study);
  my $attributes = $cfg->{attributes};
  my $rank = 1;
  for my $attribute (@$attributes) {
    $attribute->{name} //= camelHeader($attribute->{label});
    $attribute->{type} //= "String";
    $attribute->{name} =~ s{(^\s+|\s+$)}{};
    $attribute->{label} =~ s{(^\s+|\s+$)}{};
    $attribute->{type} = lc($attribute->{type});
    $dbh->do(qq{INSERT INTO "ATTRIBUTES" ("STUDY_ID", "NAME", "LABEL", "TYPE", "RANK") VALUES (?, ?, ?, ?, ?)}, {},
      $study_ref->{id}, $attribute->{name}, $attribute->{label}, $attribute->{type}, $rank++);

    my $attribute_ref = $dbh->selectrow_hashref(qq{SELECT * FROM "ATTRIBUTES" WHERE "NAME" = ? AND "STUDY_ID" = ?}, {}, $attribute->{name}, $study_ref->{id});
    $attribute_ref = lowercaseify($attribute_ref);
    $attribute->{id} = $attribute_ref->{id};

    ## Now, we might have attribute permissions to record, which we will process
    ## later....

    if (exists($attribute->{permission})) {
      for my $permission (@{$attribute->{permission}}) {
        foreach my $key (keys %$permission) {
          my $role = $permission->{$key};
          push @{$context->{attribute_permissions}->{$role}->{$key}}, $attribute->{name};
        }
      }
    }
  }

  my $views = $cfg->{views};
  for my $view (@$views) {
    $view->{name} //= camelHeader($view->{label});
    $logger->info("Writing view: ", $view->{label});
    $dbh->do(qq{INSERT INTO "VIEWS" ("STUDY_ID", "NAME", "DESCRIPTION") VALUES (?, ?, ?)}, {},
      $study_ref->{id}, $view->{name}, $view->{label});

    my $view_ref = $dbh->selectrow_hashref(qq{SELECT * FROM "VIEWS" WHERE "NAME" = ? AND "STUDY_ID" = ?}, {}, $view->{name}, $study_ref->{id});
    $view_ref = lowercaseify($view_ref);
    $view->{id} = $view_ref->{id};
  }

  ## Now we can build the view attribute mapping. Unless otherwise specified,
  ## an attribute is in all views.
  $logger->info("Writing view attributes for: ", $study);
  my @view_names = map { $_->{name} } @$views;
  my %view_ranks = ();
  my %view_table = ();
  for my $view (@$views) {
    $view_ranks{$view->{name}} = 1;
    $view_table{$view->{name}} = $view;
  }
  for my $attribute (@$attributes) {
    my @views = (exists($attribute->{views})) ? @{$attribute->{views}} : keys %view_table;
    for my $view (map { $view_table{$_} } @views) {
      if (! $view) {
        die("Missing view for attribute: ", @views);
      }
      $dbh->do(qq{INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (?, ?, ?)}, {},
        $view->{id}, $attribute->{id}, $view_ranks{$view->{name}}++);
    }
  }

  if (exists($context->{attribute_permissions})) {
    foreach my $role_name (keys %{$context->{attribute_permissions}}) {
      my $permissions = $context->{attribute_permissions}->{$role_name};
      foreach my $key (keys %$permissions) {
        my $values = $permissions->{$key};
        $values = join(",", @$values);
        my $permission = "attribute:$key:$values";

        ## Now let's find the role and add a new permission...
        if (exists($cfg->{roles})) {
          for my $role (@{$cfg->{roles}}) {
            if ($role->{name} eq $role_name) {
              push @{$role->{permissions}}, $permission;
              last;
            }
          }
        }
      }
    }
  }

  $logger->info("Writing roles for: ", $study);
  if (exists($cfg->{roles})) {
    my @roles = @{$cfg->{roles}};
    for my $role (@roles) {
      my $name = $role->{name};
      my $study_name = $role->{study};
      my $role_ref = $dbh->selectrow_hashref(qq{SELECT * FROM "ROLES" WHERE "NAME" = ?}, {}, $name);
      my $role_id;
      if ($role_ref) {
        $role_ref = lowercaseify($role_ref);
        $logger->info("Found existing role: ", $name);
        $logger->info("Deleting existing permissions");
        $dbh->do(qq{DELETE FROM "ROLE_PERMISSIONS" WHERE "ROLE_ID" = ?}, {}, $role_ref->{id});
        $logger->info("Deleting existing users");
        $dbh->do(qq{DELETE FROM "USER_ROLES" WHERE "ROLE_ID" = ?}, {}, $role_ref->{id});
        $role_id = $role_ref->{id};
      } else {
        $logger->info("Creating new role: ", $name);
        my $role_study_ref = $dbh->selectrow_hashref(qq{SELECT * FROM "STUDIES" WHERE "NAME" = ?}, {}, $study_name);
        if (! $role_study_ref) {
          die("Can't find existing study: $study_name");
        }
        $role_study_ref = lowercaseify($role_study_ref);
        $dbh->do(qq{INSERT INTO "ROLES" ("STUDY_ID", "NAME") VALUES (?, ?)}, {}, $role_study_ref->{id}, $name);
        $role_ref = $dbh->selectrow_hashref(qq{SELECT * FROM "ROLES" WHERE "NAME" = ? AND "STUDY_ID" = ?}, {}, $name, $role_study_ref->{id});
        if (! $role_ref) {
          die("Can't find newly created role study: name");
        }
        $role_ref = lowercaseify($role_ref);
        $role_id = $role_ref->{id};
      }

      $DB::single = 1 if (! $role_id);

      if (exists($role->{users})) {
        for my $user (@{$role->{users}}) {
          $logger->info("Adding user: ", $user);
          $dbh->do(qq{INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES (?, ?)}, {}, $user, $role_id);
        }
      }

      if (exists($role->{permissions})) {
        for my $permission (@{$role->{permissions}}) {
          $logger->info("Adding permission: ", $permission);
          $dbh->do(qq{INSERT INTO "ROLE_PERMISSIONS" ("PERMISSION", "ROLE_ID") VALUES (?, ?)}, {}, $permission, $role_id);
        }
      }
    }
  }

  $logger->info("Committing transaction");
  $dbh->commit();

  $logger->info("Disconnecting from the database");
  $dbh->disconnect();
}

write_data($context, $cfg);

1;

__END__

=head1 NAME

create.pl - Script to create a study

=head1 SYNOPSIS

sample [options] [file ...]
 Options:
   -help            brief help message
   -man             full documentation

=head1 OPTIONS

=over 8

=item B<-help>

Print a brief help message and exits.

=item B<-man>

Prints the manual page and exits.

=back

=head1 DESCRIPTION

B<This program> will read the given input file(s) and do something
useful with the contents thereof.

=cut
