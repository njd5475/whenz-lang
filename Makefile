
target := whenz-lang
mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
current_dir := $(notdir $(patsubst %/,%,$(dir $(mkfile_path))))

.PHONY: default
default: all

clean:
	rm -rf ./build/distributions
	rm -rf $(HOME)/bin/Whenz
	rm -rf $(HOME)/bin/whenz
	rm -rf $(HOME)/bin/whenz-lang

$(HOME)/bin:
	echo No home, lets make a nice home!
	echo mkdir $(HOME)/bin
	mkdir $(HOME)/bin

./build/distributions/$(target).zip:
	./gradlew distZip

./build/distributions/$(target): ./build/distributions/$(target).zip
	cd ./build/distributions/ && unzip $(target).zip

daily-version:
	@ruby -e 'puts (Time.now.utc.to_i / 86400)'

$(HOME)/bin/whenz: $(HOME)/bin ./build/distributions/$(target)
	cp -R ./build/distributions/$(target) $(HOME)/bin/$(target)
	ln -s $(HOME)/bin/$(target)/bin/$(target) $(HOME)/bin/whenz

all: $(HOME)/bin/whenz
