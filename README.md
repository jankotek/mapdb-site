This is www.mapdb.org website and documentation. It uses Sphinx to generate HTML, PDF and ebook.

To compile install newest Sphinx, version bundled with Ubuntu 14.04 is too old:

    sudo apt-get install python-pip
    sudo pip install sphinx
    sudo pip install ablog

Documentation uses some files from mapdb. So checkout source repository to ../mapdb

To generate PDF one has to install texlive packages.

    sudo apt-get install texlive-latex-base texlive-fonts-recommended texlive-latex-extra --no-install-recommends

Script also uses Maven to execute some code and verify tests. 

    sudo apt-get install maven
    
It might also depend on latest version of MapDB, so install it into local maven repo:
    
    sudo apt-get install git
    git clone https://github.com/jankotek/mapdb.git
    cd mapdb
    mvn install -DskipTests=true

And you will need Dokka library (javadoc for Kotlin). Download [dokka jar](https://github.com/Kotlin/dokka/releases) and save it under relative path: `../bin/dokka-fatjar.jar`

You might also have to set java home:

    export JAVA_HOME=/opt/jdk8

Finally generate html:

    ./make.sh

And publish it (if you have a rights)

    ./publish.sh

