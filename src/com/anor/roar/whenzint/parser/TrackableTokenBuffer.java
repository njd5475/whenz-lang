package com.anor.roar.whenzint.parser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

public class TrackableTokenBuffer implements TokenBuffer {

  private LinkedList<Token>   track;
  private ListIterator<Token> iter;
  private TokenBuffer         buffer;

  public TrackableTokenBuffer(TokenBuffer buffer) {
    if (buffer == null) {
      throw new NullPointerException(
          "TrackableTokenBuffer needs a buffer to get tokens from!");
    }
    this.buffer = buffer;
  }

  /**
   * Mark starts recording every token taken from buffer. This will blow away
   * any previous tracked tokens;
   */
  public void mark() {
    if (track == null) {
      track = new LinkedList<Token>();
      iter = null;
    }
  }

  /**
   * Set the iterator
   */
  public void rewind() {
    iter = track.listIterator();
  }

  /**
   * Start removing from the buffer again no more tokens.
   */
  public void unmark() {
    iter = null;
  }

  public Token take() throws IOException {
    if (isTracking()) {
      if (isReplaying()) {
        return iter.next();
      } else {
        unmark();
        Token t = buffer.take();
        track.add(t);
        return t;
      }
    } else {
      return buffer.take();
    }
  }

  private boolean isReplaying() {
    return iter != null && iter.hasNext();
  }

  private boolean isTracking() {
    return track != null;
  }

  @Override
  public Token peek() throws IOException {
    if(isReplaying()) {
      Token next = iter.next();
      iter.previous();
      return next;
    }
    return buffer.peek();
  }

  @Override
  public boolean isEmpty() {
    return buffer.isEmpty();
  }

  public static final TrackableTokenBuffer wrap(TokenBuffer buffer) {
    return new TrackableTokenBuffer(buffer);
  }
}
