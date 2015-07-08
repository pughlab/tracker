#!/usr/bin/env perl -w

use strict; 
use warnings;

use Getopt::Long;
use LWP::UserAgent;
use HTTP::Cookies;
use JSON::XS;

my $help = 0;
my $url;
my $value;
my $username;
my $password;

GetOptions(
  'help|?' => \$help, 
  'url=s' => \$url,
  'value=s' => \$value,
  'username=s' => \$username,
  'password=s' => \$password, 
) or die("Error in command line arguments\n");

sub write {

}

write();

1;


__END__

=head1 NAME

write.pl - Script to write a value into a tracker cell

=head1 SYNOPSIS

sample [options] [file ...]
 Options:
   --help                brief help message
   --url=xxx             the URL of the tracker element to write
   --value=xxx           the value to write
   --username=xxx        the username
   --password=xxx        the password

=head1 DESCRIPTION

B<This program> writes a new value into a tracker cell. It isn't a very
complete or powerful script, but it does illustrate how the tracker's
RESTful API can be integrated into other scripts. 


=head1 OPTIONS

=over 8

=item B<-help>

Print a brief help message and exits.

=item B<-man>

Prints the manual page and exits.

=back

=cut