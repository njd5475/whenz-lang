#!/usr/bin/env ruby
require 'open3'

simple = true
verbose = false
passed = []
errors = []
number = 1
Dir.glob %w(. scripts ** Test*.whenz).join('/')  do |file|
  puts "Test ##{number} - Running test file #{file}" if verbose
  result, err, status = Open3.capture3("java -jar build/libs/*.jar #{file}")
  if result =~ /Test passed/ then
    puts "Test passes" if verbose
    print "." if simple
    passed << file
  else
    print "F" if simple
    puts "Output: #{result}" if verbose
    errors << [file, result, err, status, number]
  end

  number = number + 1
end

puts ""
puts "Out of #{number-1} tests, #{passed.length} passed, #{errors.length} failed"

puts "\rFailed Tests:"
errors.each do |file, result, err, status, number|
  puts "Test ##{number} at #{file}, failed"
  puts "Output: "
  puts err
  puts "\r\r"
end
