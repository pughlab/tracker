#!/usr/bin/env perl -w

use strict;
use warnings;

use common::sense;

use Spreadsheet::XLSX;
use Text::Iconv;
use XML::Entities;
use String::CamelCase qw(camelize);
use Time::Piece;
use DateTime;
use DateTime::Duration;
use DateTime::Format::Excel;
use DateTime::Format::Natural;
use DateTime::Format::ISO8601;
use Text::Levenshtein::XS qw(distance);
use List::MoreUtils qw(first_index all);
use Algorithm::Diff qw(traverse_sequences);
use DBI;
use JSON::XS qw(encode_json);

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
my $overwrite;
my $config = 'import.yml';
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

foreach my $file (@{$cfg->{files}}) {
  extract($context, $cfg, $file);
}
merge_data($context, $cfg);
write_data($context, $cfg);

sub parse_date {
  my ($string) = @_;
  my @formats = (
    '%e/%b/%Y %H:%M',
    '%e-%b-%Y',
    '%e/%b/%Y',
    '%e-%b-%y',
    '%e/%b/%y',
    '%e %b %Y'
  );

  $string =~ s{ +}{}g;

  ## Apply some crappy fixes
  $string =~ s{\bsept\b}{sep}i;

  my $date = undef;
  foreach my $format (@formats) {
    eval {
      ## I really hate this!
      local $SIG{__WARN__} = sub { };
      $date = Time::Piece->strptime($string, $format);
    };
    if (defined($date)) {

      if ($date->year() < 1990) {
        $date = undef;
        next;
      }

      ## See if we are almost the same as the original
      my $reformatted = $date->strftime($format);

      if (distance($string, $reformatted) >= 10) {
        $date = undef;
        next;
      }

      return $date->datetime();
    }
  }

  return;
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

sub extract {
  my ($context, $cfg, $file) = @_;
  my $notes = $cfg->{$file};
  my $converter = Text::Iconv->new ("utf-8", "windows-1251");

  $logger->info("Opening spreadsheet: $file");
  my $excel   = Spreadsheet::XLSX->new($file, $converter);

  my @headers = ();
  my @records = ();
  my $headerNames = {};

  for my $worksheet (@{$excel->{Worksheet}}) {
    next unless ($worksheet->{Name} eq $notes->{sheet});
    $logger->info("Reading data from sheet: $worksheet->{Name}");
    my $row_min = $worksheet->{MinRow};
    my $row_max = $worksheet->{MaxRow};
    my $col_min = $worksheet->{MinCol};
    my $col_max = $worksheet->{MaxCol};

    $logger->info("Reading rows: $row_min to $row_max, columns: $col_min to $col_max");

    for my $col ($col_min .. $col_max) {
      my $cell = $worksheet->{Cells}[$row_min][$col];
      my $value = $cell->{Val};
      $value = XML::Entities::decode('all', $value) if (defined($value));

      $logger->warn("Missing header cell value: column: $col") if (! defined($value));
      my $label = $value // "Unknown $col";
      my $name = camelHeader($label);
      push @headers, $name;
      $headerNames->{$name} = $label;
    }

    for my $row ($row_min + 1 .. $row_max) {
      # last if ($row > 100);

      my $record = {};
      my $values = '';
      for my $col ($col_min .. $col_max) {
        my $cell = $worksheet->{Cells}[$row][$col];
        my $value = $cell->{Val};
        next if ($value =~ m{<row });
        $value = XML::Entities::decode('all', $value) if (defined($value));

        my $attribute = $headers[$col];
        $record->{$attribute} = $value;
        $values ||= $value;
      }
      if (! $values) {
        $logger->warn("Skipping blank record");
        next;
      }

      push @records, $record;
    }
  }

  if (@records == 0) {
    croak("Failing because no records loaded from: $file");
  }

  my $headerTypes = {};
  my $headerMap = {};

  ## Add the fixed headers::
  my $fixed = $cfg->{$file}->{fixed};
  if (defined($fixed)) {
    while(my ($k, $v) = each %{$cfg->{$file}->{fixed}}) {
      push @headers, $k;
    }
  }

  ## Now we should analyse the data and map it to the context.
  for my $header (@headers) {
    my $mapped = $notes->{fieldmap}->{$header};
    if (! $mapped) {
      my $index = first_index { $_ eq $header } @{$cfg->{fields}};
      if ($index != -1) {
        $mapped = $header;
      };
    }
    if ($mapped) {
      $logger->trace("Header ", $header, " mapped to ", $mapped);
      $headerMap->{$header} = $mapped;
    } else {
      $logger->warn("Can't map header ", $header, " ignoring column");
      next;
    }

    my $type = $cfg->{types}->{$mapped};
    if (! $type) {
      $logger->warn("No type for column ", $mapped);
      next;
    }

    $logger->trace("Mapping type: ", $mapped, ' to ', $type // 'undef');
    $headerTypes->{$mapped} = $type;
  }

  foreach my $record (@records) {

    my $data = {};
    foreach my $header (@headers) {
      my $mapped = $headerMap->{$header};
      next unless ($mapped);

      my $type = $headerTypes->{$mapped};
      my $value = $record->{$header};

      if ($type eq 'Date') {
        $value =~ s{^\s+}{};
        $value =~ s{\s+$}{};

        $logger->trace("Value: ", $value // 'undef', ' to ', $mapped);
        my $matchers = $cfg->{matchers}->{$mapped};
        if (defined($value) && ref($matchers) eq 'ARRAY') {
          foreach my $matcher (@$matchers) {
            my $original = $value;
            my $copy = $original;
            if ($copy =~ s/$matcher->{pattern}/$matcher->{result}/ee) {
              $value = $copy;
              if (defined($matcher->{target})) {
                $copy = $original;
                $copy =~ s/$matcher->{pattern}/$matcher->{target_result}/ee;
                $copy = {'$notAvailable' => 1} if (lc($copy) eq 'n/a');
                $data->{$matcher->{target}} = $copy;
              }
              $DB::single = 1 if ($value eq '$1');
              last;
            }
          }
        }

        if ($value =~ m{^[\d\.]+$}) {
          my $parsed_value = DateTime::Format::Excel->parse_datetime($value)->iso8601();
          $logger->trace("Parsed Excel date ", $value, ' to ', $parsed_value);
          $value = $parsed_value;
        } elsif ($value eq '') {
          $value = undef;
        } elsif (lc($value) eq 'unknown') {
          $value = undef;
        } elsif (lc($value) eq 'n/a' || $value =~ m{^-+$}) {
          $value = {'$notAvailable' => 1};
        } elsif (my $parsed = parse_date($value)) {
          $logger->trace("Parsed string date: $value -> $parsed");
          my $parsed_value = DateTime::Format::ISO8601->parse_datetime($parsed)->iso8601();
          $value = $parsed_value;
        } elsif ($value) {
          $logger->warn("Got unexpected date: ", $value, ' in field: ', $mapped);
          $value = undef;
        }
        $value = substr($value, 0, 10) if (defined($value) && ! ref($value));
        # my $date = parse_date($value);
        # $date = $date->ymd() if (ref($date) && $date->isa('Time::Piece'));
      } elsif ($type eq 'Boolean') {
        $value =~ s{^\s+}{};
        $value =~ s{\s+$}{};
        if ($value =~ m{^(?:yes|no)$}i) {
          $value = ($value =~ m{^yes$}i ? 1 : 0);
        } elsif ($value =~ m{^(?:n/a)$}i) {
          $value = undef;
        } elsif (lc($value) eq 'unknown') {
          $value = undef;
        } elsif (lc($value) eq 'n/a') {
          $value = {'$notAvailable' => 1};
        } elsif ($value) {
          $logger->warn("Got unexpected boolean value: ", $value, ' in field: ', $mapped);
          $value = undef;
        }
      } elsif ($type eq 'String') {
        ## Nothing to do here...
      } else {
        $logger->error("No type for field: ", $mapped);
      }

      $data->{$mapped} = $value;
    }

    ## Now we might have accumulated some additional crap to drop in a notes field. If so, we
    ## can do that.

    my $fixed = $cfg->{$file}->{fixed};
    if (defined($fixed)) {
      while(my ($k, $v) = each %{$cfg->{$file}->{fixed}}) {
        $data->{$k} //= $v;
      }
    }

    $record = $data;
  }


  ## Now we can process things. First, we should do something sensible to the
  ## headers

  push @{$context->{files}}, $file;
  $context->{$file}->{mapped_headers} = [ grep { defined($_) } map { $headerMap->{$_} } @headers ];
  $context->{$file}->{mapped_header_types} = $headerTypes;
  $context->{$file}->{records} = \@records;
}

sub matches {
  my ($record1, $record2, @fields) = @_;
  return all { $record1->{$_} eq $record2->{$_} } @fields;
}

sub merge_data {
  my ($context, $cfg) = @_;

  my @files = @{$context->{files}};

  my @all_records = ();
  my @all_headers = ();
  my %all_header_types = ();

  ## The challenge is to merge data from different sheets. This is partly due to the need to model
  ## views as having different things to display. Probably we need view row filters sooner rather
  ## than later.
  ##
  ## This mainly comes down to whether to update an existing record or create a new one.

  for my $file (@{$context->{files}}) {
    $logger->info("Merging data from: $file");
    my @records = @{$context->{$file}->{records}};
    my @headers = @{$context->{$file}->{mapped_headers}};

    my $merge = $cfg->{$file}->{merge_on};
    if (! defined($merge)) {
      $logger->error("No merge definitions, skipping entire file: $file");
      next;
    }

    if (! @all_headers) {
      @all_headers = @headers;
      %all_header_types = %{$context->{$file}->{mapped_header_types}}
    } else {

      while(my ($k, $v) = each %{$context->{$file}->{mapped_header_types}}) {
        $all_header_types{$k} //= $v;
      }

      my @merged = ();
      my $match_call = sub { my ($a, $b) = @_; push @merged, $all_headers[$a]; };
      my $discard_a_call = sub { my ($a, $b) = @_; push @merged, $all_headers[$a]; };
      my $discard_b_call = sub { my ($a, $b) = @_; push @merged, $headers[$b]; };
      traverse_sequences \@all_headers, \@headers, {MATCH => $match_call, DISCARD_A => $discard_a_call, DISCARD_B => $discard_b_call};

      @all_headers = @merged;
    }

    ## Note that we should never merge within a file. Do this by queueing new records and
    ## adding them at the end.
    my @records_to_add = ();
    foreach my $record (@records) {
      my $found = first_index { matches($_, $record, @$merge); } @all_records;
      if ($found == -1) {
        push @records_to_add, $record;
      } else {
        $found = $all_records[$found];
        $logger->trace("Merging records on: ", join(', ', map { $found->{$_} } @$merge ));
        while(my ($k, $v) = each %$record) {
          my $old = $found->{$k};
          if (defined($old)) {
            if ($old ne $v) {
              $logger->error("Merge value mismatch: $old, $v, for ", $k, ' in ', join(', ', map { $found->{$_} } @$merge ))
            }
          } else {
            $found->{$k} = $v;
          }
        }
      }
    }

    push @all_records, @records_to_add;
  }

  $context->{all_headers} = $cfg->{fields};
  $context->{all_records} = \@all_records;
  $context->{all_header_types} = \%all_header_types;
}


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

  # $dbh->sqlite_trace(sub {my ($statement) = @_; $logger->info("SQL: ", $statement); });

  ## First find the study
  my $study_ref = $dbh->selectrow_hashref(qq{SELECT * FROM "studies" WHERE "name" = ?}, {}, $study);

  if ($study_ref) {
    $study_ref = lowercaseify($study_ref);
    $logger->info("Found existing study: ", $study);
  }

  ## If there isn't a study, we can create one
  if (! $study_ref) {
    $dbh->do(qq{INSERT INTO studies (name) VALUES (?)}, {}, $study);
    $study_ref = $dbh->selectrow_hashref(qq{SELECT * FROM studies WHERE name = ?}, {}, $study);
    $study_ref = lowercaseify($study_ref);
    $logger->info("Created new study: ", $study);
  }

  ## Right. Now let's create the attributes. But first of all, if we're overwriting, delete everything
  if ($cfg->{overwrite}) {
    $logger->info("Overwrite selected: deleting view attributes");
    $dbh->do(qq{DELETE FROM view_attributes WHERE view_id IN (SELECT id FROM views WHERE study_id = ?)}, {}, $study_ref->{id});

    $logger->info("Overwrite selected: deleting views");
    $dbh->do(qq{DELETE FROM views WHERE study_id = ?}, {}, $study_ref->{id});

    $logger->info("Overwrite selected: deleting attributes");
    $dbh->do(qq{DELETE FROM attributes WHERE study_id = ?}, {}, $study_ref->{id});

    $logger->info("Overwrite selected: deleting case values");
    $dbh->do(qq{DELETE FROM case_attribute_strings WHERE case_id IN (select id FROM cases WHERE study_id = ?)}, {}, $study_ref->{id});
    $dbh->do(qq{DELETE FROM case_attribute_dates WHERE case_id IN (select id FROM cases WHERE study_id = ?)}, {}, $study_ref->{id});
    $dbh->do(qq{DELETE FROM case_attribute_booleans WHERE case_id IN (select id FROM cases WHERE study_id = ?)}, {}, $study_ref->{id});

    $logger->info("Overwrite selected: deleting cases");
    $dbh->do(qq{DELETE FROM cases WHERE study_id = ?}, {}, $study_ref->{id});
  }

  my $headers = $context->{all_headers};
  my $header_types = $context->{all_header_types};
  my $records = $context->{all_records};

  $logger->info("Writing attributes");

  my $attribute_ids = {};
  my $attribute_types = {};
  my $index = 0;

  ## So now we can begin the other stuff
  for my $attribute (@$headers) {
    my $label = $attribute;
    my $type = lc($header_types->{$attribute});
    my $options = $cfg->{options}->{$attribute};
    $dbh->do(qq{INSERT INTO attributes (study_id, name, label, type, rank, options) VALUES (?, ?, ?, ?, ?, ?)}, {}, $study_ref->{id}, $attribute, $label, $type, $index++, $options);
    my $attribute_record = $dbh->selectrow_hashref(qq{SELECT * FROM attributes WHERE study_id = ? AND name = ?}, {}, $study_ref->{id}, $attribute);
    $attribute_record = lowercaseify($attribute_record);
    $attribute_ids->{$attribute} = $attribute_record->{id};
  }

  ## Let's see if we can guess which is an identifier
  my $identifier_attribute_id = undef;
  for my $attribute (@$headers) {
    if ($attribute eq $cfg->{identifier_attribute} || $attribute =~ m{id|identifier}) {
      $identifier_attribute_id = $attribute_ids->{$attribute};
      last;
    }
  }

  ## And now let's add the values.
  $logger->info("Writing cases");
  my $value_index = 0;
  foreach my $case (@$records) {
    $value_index++;

    $dbh->do(qq{INSERT INTO cases (study_id) VALUES (?)}, {}, $study_ref->{id});
    my $case_id = $dbh->last_insert_id(undef, undef, undef, undef);

    for my $attribute (@$headers) {
      my $type = lc($header_types->{$attribute});
      if (! $type) {
        croak("Invalid type for attribute: $attribute");
      }
      my $table = "case_attribute_${type}s";
      my $sql = $dbh->quote_identifier($table);
      my $value = $case->{$attribute};
      next if (! defined($value));

      my $not_available = 0;
      if (ref($value) && $value->{'$notAvailable'}) {
        $not_available = 1;
        $value = undef;
      } elsif (ref($value)) {
        carp("Invalid value: $value");
      }

      $logger->trace("Values: ", join(', ', $case_id, $attribute_ids->{$attribute}, $value, localtime(), "load", $not_available));
      my $time = localtime();
      eval {
        $dbh->do(qq{INSERT INTO $sql (case_id, attribute_id, value, not_available) VALUES (?, ?, ?, ?)}, {},
          $case_id, $attribute_ids->{$attribute}, $value, $not_available);
      };
      my $error = $@;
      if ($error) {
        die("Error writing $case_id, $attribute, $value: $error");
      }
    }
  }

  ## And finally, create a basic view containing all the attributes
  $logger->info("Writing primary view");
  $dbh->do(qq{INSERT INTO views (study_id, name) VALUES (?, 'primary')}, {}, $study_ref->{id});
  my $view_ref = $dbh->selectrow_hashref(qq{SELECT * FROM views WHERE study_id = ? AND name = 'primary'}, {}, $study_ref->{id});
  $view_ref = lowercaseify($view_ref);
  my $rank = 1;
  for my $attribute (@$headers) {
    $dbh->do(qq{INSERT INTO view_attributes (view_id, attribute_id, rank) VALUES (?, ?,  ?)}, {}, $view_ref->{id}, $attribute_ids->{$attribute}, $rank++);
  }

  $logger->info("Committing transaction");
  $dbh->commit();

  $logger->info("Disconnecting from the database");
  $dbh->disconnect();
}

1;

__END__

=head1 NAME

load.pl - Script to load a spreadsheet into a study

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
