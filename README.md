# whenz-lang
A simple expiremental outside the box language, built to remove traditional concerns.

# Mantra
@creator: It's not the amount of work a programmer does that makes him efficient. 
It's the smallest amount of work a programmer has to do that makes him efficient.

# Simple Explanation of structure

```
// comments are simple
when <condition> do once?
	<action>
	...
	<action>
```

# Hello World

```
#!/usr/bin/env whenz

when event app_starts do once
	print Hello World!
	
```

# Variables

```
	//atm just module global variables
	@<identifier>.<identifier>...<identifier>
```

# Events

When the program starts the first event is `app_starts`. You can trigger any event using the `trigger <event>` action.

And in any condition you can trigger actions by `when event <event> do`. 

# States

Default variable states `set` and `changed`. On initial assignment the variable will be in the `set` state.

You can change the state of a variable by the action `@variable is <state>`. States can be used in any `<condition>` statement.

# Actions

Print to standard output

```
	// Prints everything after the print to the end of the statement trims whitespace
	print <to_print> <endline>
	
	// Print will print variables within the statement
	print <to_print> @variable <to_print>
```

Command execution

```
	execute <command> <endline>
```

with monitoring

```
	monitor as @command exec <command> <endline>
```

# Installation

Unzip or extract the tarball whereever you'd like it. Add softlink in `/usr/local/bin/`

		$ ln -s <extract_dir>/whenz-lang/bin/whenz-lang /usr/local/bin/whenz
		
With this added you can now use the language as a scripting language on any linux system.

# Motivations and ideas
The language is based around a couple of key concepts. One of those being that you only really need a single loop in any program. There is no need for a program to enter multiple loops. Nor is there a need to halt any processing of other logic while you are in any particular branch of the code.

The language also throws away the idea of there being a call stack. Think about all the languages out there with call stacks. They have grown to a rediculuous number of function calls and it's only getting worse. Programming languages use this model as a means to allow a programmer to split up the code and makes maintenance easier. But programming languages force this as the only way to write code and it has runtime implications. Why as a programmer though would I want my program to run poorly because I need to maintain the code. The code structure, source layout, and format should not impact the runtime performance of my code.

What is it that I want to do when I sit down to add a new function or a new piece of code to a program. What has changed. I have a new condition or want to add new code that handles a new functionality. Lets imagine the typical types of things a programmer might need to add on a daily basis. Add a new button that when clicked does something, add another catch for some error case that you didn't know existed. Trace a particular set of conditions and change the way calculations or data is changing because of those conditions. 

This is very indepth but if you don't have cohesive code you could end up adding the exact same check to many many different places in the code because you created branches to handle different types of situations. So one section of code is used for mobile, another for desktop, and yet another for tablet. Same goes for operating systems. A lot of times you make different sections of code for Windows and then for OSX or Linux. These all create separate branches and depending on how you've extracted these concerns from the code you could end up with tangles of interweaving code that conflict. Whenz sets up to make the separation easier and development should not depend on knowing huge code paths. There should be tools that help you analyze those paths when you need to and what was triggered when.




