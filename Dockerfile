FROM ubuntu:14.04
#Needs Java 8, make, git
#http://askubuntu.com/questions/464755/how-to-install-openjdk-8-on-14-04-lts
RUN 	apt-get -y -q update &&\
	apt-get -y -q install software-properties-common &&\
	add-apt-repository ppa:webupd8team/java	&&\
	apt-get -y update &&\
	echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections &&\
	apt-get -y -q install make git oracle-java8-installer &&\
	update-java-alternatives -s java-8-oracle 
#Needs Maven 3.1+
RUN	apt-get install -y -q gdebi &&\
	wget http://ppa.launchpad.net/natecarlson/maven3/ubuntu/pool/main/m/maven3/maven3_3.2.1-0~ppa1_all.deb &&\
	gdebi --non-interactive maven3_3.2.1-0~ppa1_all.deb &&\
	ln -s /usr/share/maven3/bin/mvn /usr/bin/mvn
#clone the 'develop' branch on the tracker repo and install
RUN 	git clone https://github.com/pughlab/tracker.git &&\
	cd tracker &&\
	mvn install

#Bind the tracker to port 9999 and launch the webapp

CMD	cd tracker && mvn -Djetty.port=9999 jetty:run
