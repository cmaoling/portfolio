// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: client.proto

package name.abuchen.portfolio.model.proto.v1;

/**
 * Protobuf type {@code name.abuchen.portfolio.PExchangeRate}
 */
public final class PExchangeRate extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:name.abuchen.portfolio.PExchangeRate)
    PExchangeRateOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PExchangeRate.newBuilder() to construct.
  private PExchangeRate(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PExchangeRate() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PExchangeRate();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return name.abuchen.portfolio.model.proto.v1.ClientProtos.internal_static_name_abuchen_portfolio_PExchangeRate_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return name.abuchen.portfolio.model.proto.v1.ClientProtos.internal_static_name_abuchen_portfolio_PExchangeRate_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            name.abuchen.portfolio.model.proto.v1.PExchangeRate.class, name.abuchen.portfolio.model.proto.v1.PExchangeRate.Builder.class);
  }

  public static final int DATE_FIELD_NUMBER = 1;
  private long date_ = 0L;
  /**
   * <code>int64 date = 1;</code>
   * @return The date.
   */
  @java.lang.Override
  public long getDate() {
    return date_;
  }

  public static final int VALUE_FIELD_NUMBER = 2;
  private name.abuchen.portfolio.model.proto.v1.PDecimalValue value_;
  /**
   * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
   * @return Whether the value field is set.
   */
  @java.lang.Override
  public boolean hasValue() {
    return value_ != null;
  }
  /**
   * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
   * @return The value.
   */
  @java.lang.Override
  public name.abuchen.portfolio.model.proto.v1.PDecimalValue getValue() {
    return value_ == null ? name.abuchen.portfolio.model.proto.v1.PDecimalValue.getDefaultInstance() : value_;
  }
  /**
   * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
   */
  @java.lang.Override
  public name.abuchen.portfolio.model.proto.v1.PDecimalValueOrBuilder getValueOrBuilder() {
    return value_ == null ? name.abuchen.portfolio.model.proto.v1.PDecimalValue.getDefaultInstance() : value_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (date_ != 0L) {
      output.writeInt64(1, date_);
    }
    if (value_ != null) {
      output.writeMessage(2, getValue());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (date_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(1, date_);
    }
    if (value_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getValue());
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof name.abuchen.portfolio.model.proto.v1.PExchangeRate)) {
      return super.equals(obj);
    }
    name.abuchen.portfolio.model.proto.v1.PExchangeRate other = (name.abuchen.portfolio.model.proto.v1.PExchangeRate) obj;

    if (getDate()
        != other.getDate()) return false;
    if (hasValue() != other.hasValue()) return false;
    if (hasValue()) {
      if (!getValue()
          .equals(other.getValue())) return false;
    }
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + DATE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getDate());
    if (hasValue()) {
      hash = (37 * hash) + VALUE_FIELD_NUMBER;
      hash = (53 * hash) + getValue().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(name.abuchen.portfolio.model.proto.v1.PExchangeRate prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code name.abuchen.portfolio.PExchangeRate}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:name.abuchen.portfolio.PExchangeRate)
      name.abuchen.portfolio.model.proto.v1.PExchangeRateOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return name.abuchen.portfolio.model.proto.v1.ClientProtos.internal_static_name_abuchen_portfolio_PExchangeRate_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return name.abuchen.portfolio.model.proto.v1.ClientProtos.internal_static_name_abuchen_portfolio_PExchangeRate_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              name.abuchen.portfolio.model.proto.v1.PExchangeRate.class, name.abuchen.portfolio.model.proto.v1.PExchangeRate.Builder.class);
    }

    // Construct using name.abuchen.portfolio.model.proto.v1.PExchangeRate.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      date_ = 0L;
      value_ = null;
      if (valueBuilder_ != null) {
        valueBuilder_.dispose();
        valueBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return name.abuchen.portfolio.model.proto.v1.ClientProtos.internal_static_name_abuchen_portfolio_PExchangeRate_descriptor;
    }

    @java.lang.Override
    public name.abuchen.portfolio.model.proto.v1.PExchangeRate getDefaultInstanceForType() {
      return name.abuchen.portfolio.model.proto.v1.PExchangeRate.getDefaultInstance();
    }

    @java.lang.Override
    public name.abuchen.portfolio.model.proto.v1.PExchangeRate build() {
      name.abuchen.portfolio.model.proto.v1.PExchangeRate result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public name.abuchen.portfolio.model.proto.v1.PExchangeRate buildPartial() {
      name.abuchen.portfolio.model.proto.v1.PExchangeRate result = new name.abuchen.portfolio.model.proto.v1.PExchangeRate(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(name.abuchen.portfolio.model.proto.v1.PExchangeRate result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.date_ = date_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.value_ = valueBuilder_ == null
            ? value_
            : valueBuilder_.build();
      }
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof name.abuchen.portfolio.model.proto.v1.PExchangeRate) {
        return mergeFrom((name.abuchen.portfolio.model.proto.v1.PExchangeRate)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(name.abuchen.portfolio.model.proto.v1.PExchangeRate other) {
      if (other == name.abuchen.portfolio.model.proto.v1.PExchangeRate.getDefaultInstance()) return this;
      if (other.getDate() != 0L) {
        setDate(other.getDate());
      }
      if (other.hasValue()) {
        mergeValue(other.getValue());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              date_ = input.readInt64();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              input.readMessage(
                  getValueFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private long date_ ;
    /**
     * <code>int64 date = 1;</code>
     * @return The date.
     */
    @java.lang.Override
    public long getDate() {
      return date_;
    }
    /**
     * <code>int64 date = 1;</code>
     * @param value The date to set.
     * @return This builder for chaining.
     */
    public Builder setDate(long value) {

      date_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>int64 date = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearDate() {
      bitField0_ = (bitField0_ & ~0x00000001);
      date_ = 0L;
      onChanged();
      return this;
    }

    private name.abuchen.portfolio.model.proto.v1.PDecimalValue value_;
    private com.google.protobuf.SingleFieldBuilderV3<
        name.abuchen.portfolio.model.proto.v1.PDecimalValue, name.abuchen.portfolio.model.proto.v1.PDecimalValue.Builder, name.abuchen.portfolio.model.proto.v1.PDecimalValueOrBuilder> valueBuilder_;
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     * @return Whether the value field is set.
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     * @return The value.
     */
    public name.abuchen.portfolio.model.proto.v1.PDecimalValue getValue() {
      if (valueBuilder_ == null) {
        return value_ == null ? name.abuchen.portfolio.model.proto.v1.PDecimalValue.getDefaultInstance() : value_;
      } else {
        return valueBuilder_.getMessage();
      }
    }
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     */
    public Builder setValue(name.abuchen.portfolio.model.proto.v1.PDecimalValue value) {
      if (valueBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        value_ = value;
      } else {
        valueBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     */
    public Builder setValue(
        name.abuchen.portfolio.model.proto.v1.PDecimalValue.Builder builderForValue) {
      if (valueBuilder_ == null) {
        value_ = builderForValue.build();
      } else {
        valueBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     */
    public Builder mergeValue(name.abuchen.portfolio.model.proto.v1.PDecimalValue value) {
      if (valueBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          value_ != null &&
          value_ != name.abuchen.portfolio.model.proto.v1.PDecimalValue.getDefaultInstance()) {
          getValueBuilder().mergeFrom(value);
        } else {
          value_ = value;
        }
      } else {
        valueBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     */
    public Builder clearValue() {
      bitField0_ = (bitField0_ & ~0x00000002);
      value_ = null;
      if (valueBuilder_ != null) {
        valueBuilder_.dispose();
        valueBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     */
    public name.abuchen.portfolio.model.proto.v1.PDecimalValue.Builder getValueBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getValueFieldBuilder().getBuilder();
    }
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     */
    public name.abuchen.portfolio.model.proto.v1.PDecimalValueOrBuilder getValueOrBuilder() {
      if (valueBuilder_ != null) {
        return valueBuilder_.getMessageOrBuilder();
      } else {
        return value_ == null ?
            name.abuchen.portfolio.model.proto.v1.PDecimalValue.getDefaultInstance() : value_;
      }
    }
    /**
     * <code>.name.abuchen.portfolio.PDecimalValue value = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        name.abuchen.portfolio.model.proto.v1.PDecimalValue, name.abuchen.portfolio.model.proto.v1.PDecimalValue.Builder, name.abuchen.portfolio.model.proto.v1.PDecimalValueOrBuilder> 
        getValueFieldBuilder() {
      if (valueBuilder_ == null) {
        valueBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            name.abuchen.portfolio.model.proto.v1.PDecimalValue, name.abuchen.portfolio.model.proto.v1.PDecimalValue.Builder, name.abuchen.portfolio.model.proto.v1.PDecimalValueOrBuilder>(
                getValue(),
                getParentForChildren(),
                isClean());
        value_ = null;
      }
      return valueBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:name.abuchen.portfolio.PExchangeRate)
  }

  // @@protoc_insertion_point(class_scope:name.abuchen.portfolio.PExchangeRate)
  private static final name.abuchen.portfolio.model.proto.v1.PExchangeRate DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new name.abuchen.portfolio.model.proto.v1.PExchangeRate();
  }

  public static name.abuchen.portfolio.model.proto.v1.PExchangeRate getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PExchangeRate>
      PARSER = new com.google.protobuf.AbstractParser<PExchangeRate>() {
    @java.lang.Override
    public PExchangeRate parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<PExchangeRate> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PExchangeRate> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public name.abuchen.portfolio.model.proto.v1.PExchangeRate getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
