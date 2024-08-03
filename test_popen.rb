#!/usr/bin/env ruby
require 'open3'

cmd = ARGV.join(" ")

puts "Executing #{cmd}"
Open3.popen2e(cmd) do |stdin, stdout_stderr, wait_thread|
  Thread.new do
    stdout_stderr.each {|l| puts l }
  end

  stdin.puts 'ls'
  stdin.close

  wait_thread.value
end

