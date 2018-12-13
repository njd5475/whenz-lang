
target := whenz-lang
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

./build/distributions/$(target).zip:
	./gradlew distZip

./build/distributions/$(target): ./build/distributions/$(target).zip
	cd ./build/distributions/ && unzip $(target).zip

$(HOME)/bin/whenz: $(HOME)/bin ./build/distributions/$(target)
	cp -R ./build/distributions/$(target)/ $(HOME)/bin/.
	ln -s $(HOME)/bin/$(target)/bin/whenz-lang $(HOME)/bin/whenz

all: $(HOME)/bin/whenz
