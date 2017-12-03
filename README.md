# play-java-streaming-example 

This is an example Play template that demonstrates Streaming with Server Sent Events using Akka Streams.

To post message use example link shown below where `msg` param that is eq to `123` is a new message that will be posted as new event to event source
    `uid` is user id
    
```
/java/eventSource/post?msg=123&uid=456
```

To read events from eventSource use link below where `123` is user id
```
/java/eventSource/live/123
```