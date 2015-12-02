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

my $help = 0;
my $file;
my $study;
my $config = 'create.yml';
my $sheet = "Sheet1";
my $database;
my $user;
my $password;

GetOptions(
  'help|?' => \$help,
  'config=s' => \$config,
) or die("Error in command line arguments\n");

if (! -e $config) {
  die("Can't find config file: $config");
}

my $logger = get_logger();
$logger->info("Reading config file: $config");
my $cfg = Config::Any->load_files({files => [$config], use_ext => 1});
$cfg = $cfg->[0];
my ($filename) = keys %$cfg;
$cfg = $cfg->{$filename};

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
