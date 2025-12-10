package com.ror.util;

//Queue implementation with generics for type class safety
public class LinkedQueue<A> {
  private static class Node<A> {

    A value;
    Node<A> next;

    Node(A value) {
      this.value = value;
    }
  }

  private Node<A> head;
  private Node<A> tail;
  private int _size = 0;

  public void enqueue(A value) {
    Node<A> newNode = new Node<>(value);
    if (tail != null) {
      tail.next = newNode;
    }
    tail = newNode;
    if (head == null) {
      head = newNode;
    }
    _size++;
  }

  public A dequeue() {
    if (head == null) return null;
    A value = head.value;
    head = head.next;
    if (head == null) tail = null;
    _size--;
    return value;
    }

    public boolean isEmpty() {
        return _size == 0;
    }
}
