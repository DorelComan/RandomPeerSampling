package de.tum.group34.mock;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FileRegion;
import io.netty.util.concurrent.EventExecutorGroup;
import io.reactivex.netty.channel.AllocatingTransformer;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.channel.ContentSource;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import org.mockito.Mockito;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

class MockWriteAndFlushOnEachConnection extends Connection<ByteBuf, ByteBuf> {

  private BehaviorSubject<ByteBuf> incomingMessage = BehaviorSubject.create();
  private ContentSource<ByteBuf> contentSource;

  public MockWriteAndFlushOnEachConnection() {
    super(Mockito.mock(Channel.class));

    final Func1<Subscriber<? super ByteBuf>, Object> subscriptionEventFactory =
        subscriber -> incomingMessage;

    ChannelPipeline mockChannelPipeline = Mockito.mock(ChannelPipeline.class);
    Channel mockChannel = Mockito.mock(Channel.class);

    Mockito.doReturn(mockChannelPipeline).when(mockChannel).pipeline();

    contentSource = new ContentSource(mockChannel, subscriptionEventFactory);
  }

  public List<ByteBuf> lastSentMessages = new ArrayList<>();

  @Override public Observable<Void> writeAndFlushOnEach(Observable<ByteBuf> msgs) {
    lastSentMessages.add(msgs.toBlocking().first());
    return Observable.just(null);
  }

  public void assertMessagesSent(int count) {
    Assert.assertEquals(count, lastSentMessages.size());
  }

  public void assertLastSentMessageEquals(ByteBuf lastMessage) {
    ByteBuf lastMsg =
        lastSentMessages.isEmpty() ? null : lastSentMessages.get(lastSentMessages.size() - 1);

    Assert.assertEquals(lastMessage, lastMsg);
  }

  public void assertMessageSent(List<ByteBuf> messages) {
    Assert.assertEquals(messages, lastSentMessages);
  }

  public void deliverIncomingMessage(ByteBuf msg) {
    incomingMessage.onNext(msg);
  }

  public void simulateIncommingSocketError(Throwable e) {
    incomingMessage.onError(e);
  }

  @Override public ContentSource<ByteBuf> getInput() {
    return contentSource;
  }

  @Override
  public <RR, WW> Connection<RR, WW> addChannelHandlerFirst(String name, ChannelHandler handler) {
    return null;
  }

  @Override
  public <RR, WW> Connection<RR, WW> addChannelHandlerFirst(EventExecutorGroup group, String name,
      ChannelHandler handler) {
    return null;
  }

  @Override
  public <RR, WW> Connection<RR, WW> addChannelHandlerLast(String name, ChannelHandler handler) {
    return null;
  }

  @Override
  public <RR, WW> Connection<RR, WW> addChannelHandlerLast(EventExecutorGroup group, String name,
      ChannelHandler handler) {
    return null;
  }

  @Override public <RR, WW> Connection<RR, WW> addChannelHandlerBefore(String baseName, String name,
      ChannelHandler handler) {
    return null;
  }

  @Override public <RR, WW> Connection<RR, WW> addChannelHandlerBefore(EventExecutorGroup group,
      String baseName, String name, ChannelHandler handler) {
    return null;
  }

  @Override public <RR, WW> Connection<RR, WW> addChannelHandlerAfter(String baseName, String name,
      ChannelHandler handler) {
    return null;
  }

  @Override public <RR, WW> Connection<RR, WW> addChannelHandlerAfter(EventExecutorGroup group,
      String baseName, String name, ChannelHandler handler) {
    return null;
  }

  @Override public <RR, WW> Connection<RR, WW> pipelineConfigurator(
      Action1<ChannelPipeline> pipelineConfigurator) {
    return null;
  }

  @Override public <RR> Connection<RR, ByteBuf> transformRead(
      Observable.Transformer<ByteBuf, RR> transformer) {
    return null;
  }

  @Override public <WW> Connection<ByteBuf, WW> transformWrite(
      AllocatingTransformer<WW, ByteBuf> transformer) {
    return null;
  }

  @Override public Observable<Void> write(Observable<ByteBuf> msgs) {
    return null;
  }

  @Override
  public Observable<Void> write(Observable<ByteBuf> msgs, Func1<ByteBuf, Boolean> flushSelector) {
    return null;
  }

  @Override public Observable<Void> writeString(Observable<String> msgs) {
    return null;
  }

  @Override public Observable<Void> writeString(Observable<String> msgs,
      Func1<String, Boolean> flushSelector) {
    return null;
  }

  @Override public Observable<Void> writeStringAndFlushOnEach(Observable<String> msgs) {
    return null;
  }

  @Override public Observable<Void> writeBytes(Observable<byte[]> msgs) {
    return null;
  }

  @Override public Observable<Void> writeBytes(Observable<byte[]> msgs,
      Func1<byte[], Boolean> flushSelector) {
    return null;
  }

  @Override public Observable<Void> writeBytesAndFlushOnEach(Observable<byte[]> msgs) {
    return null;
  }

  @Override public Observable<Void> writeFileRegion(Observable<FileRegion> msgs) {
    return null;
  }

  @Override public Observable<Void> writeFileRegion(Observable<FileRegion> msgs,
      Func1<FileRegion, Boolean> flushSelector) {
    return null;
  }

  @Override public Observable<Void> writeFileRegionAndFlushOnEach(Observable<FileRegion> msgs) {
    return null;
  }

  @Override public void flush() {
  }

  @Override public Observable<Void> close() {
    return null;
  }

  @Override public Observable<Void> close(boolean flush) {
    return null;
  }

  @Override public void closeNow() {

  }

  @Override public Observable<Void> closeListener() {
    return null;
  }
}