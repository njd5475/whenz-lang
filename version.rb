#!/usr/bin/env ruby

require 'active_support/all'

readVal = ARGV.shift if ARGV[0]

puts Time.at(readVal.to_i * 60 * 60 * 24).utc.beginning_of_day if readVal

now = Time.now.utc          # Current time

puts now.to_i / 60 / 60 / 24 if not readVal

