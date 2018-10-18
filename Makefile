
mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
current_dir := $(notdir $(patsubst %/,%,$(dir $(mkfile_path))))

.PHONY: default
default: all

clean:
	rm -rf ./build/distributions
	rm -rf $(HOME)/bin/Whenz
	rm -rf $(HOME)/bin/whenz

$(HOME)/bin:
	echo No home, let's make a nice home!
	echo mkdir $(HOME)/bin
	mkdir $(HOME)/bin

./build/distributions/Whenz.zip:
	./gradlew distZip

./build/distributions/Whenz: ./build/distributions/Whenz.zip
	cd ./build/distributions/ && unzip Whenz.zip

$(HOME)/bin/whenz: $(HOME)/bin ./build/distributions/Whenz
	cp -R ./build/distributions/Whenz/ $(HOME)/bin/.
	ln -s $(HOME)/bin/Whenz/bin/Whenz $(HOME)/bin/whenz

all: $(HOME)/bin/whenz
