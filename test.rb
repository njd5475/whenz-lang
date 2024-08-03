#!/usr/bin/env ruby
require 'open3'
require 'timeout'

simple = true
verbose = false
passed = []
errors = []
number = 1
maxTime = 10
Dir.glob %w(. scripts ** Test*.whenz).join('/')  do |file|
  puts "\nTest ##{number} - Running test file #{file}\n" if verbose
  result, err, status = [], [], 'failed'
  Open3.popen2e("java -jar build/libs/*.jar #{file}") do |i,o_e,w|
    begin
      Timeout.timeout(maxTime) do # timeout set to 10 sec, change if needed
        # process output of the process. it will produce EOF when done.
        until o_e.eof? do
          # o.read_nonblock(N) ...
          buf = o_e.read_nonblock(1024)
          result << buf
        end
      end
    rescue Timeout::Error
      # here you know that the process took longer than 10 seconds
      Process.kill("KILL", w.pid)
      err << "timed out after #{maxTime} seconds"
      # do whatever other error processing you need
    end

    w.value
  end
  result = result.join("")
  err = err.join("")
  puts "Command finished #{result} #{err} #{status}" if verbose

  if result =~ /Test passed/ then
    puts "Test passes" if verbose
    print "." if simple
    passed << file
  else
    print "F" if simple and not verbose
    puts "Output: #{result}" if verbose
    errors << [file, result, err, status, number]
  end

  number = number + 1
end

puts ""
puts "Out of #{number-1} tests, #{passed.length} passed, #{errors.length} failed"

if errors.length > 0 then
  puts "\rFailed Tests:"
  errors.each do |file, result, err, status, number|
    puts "Test ##{number} at #{file}, failed"
    puts "Output: "
    puts "Error: #{err}"
    puts "\r\r"
  end
end
