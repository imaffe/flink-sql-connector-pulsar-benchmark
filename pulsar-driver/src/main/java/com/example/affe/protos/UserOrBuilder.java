// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: resources/proto/user.proto

package com.example.affe.protos;

public interface UserOrBuilder extends
    // @@protoc_insertion_point(interface_extends:affe.User)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string name = 1;</code>
   * @return Whether the name field is set.
   */
  boolean hasName();
  /**
   * <code>optional string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional int32 age = 2;</code>
   * @return Whether the age field is set.
   */
  boolean hasAge();
  /**
   * <code>optional int32 age = 2;</code>
   * @return The age.
   */
  int getAge();
}