shrug
=====

Simple Redis-based service throttle written in Java. Testing on a Macbook Pro yielded peak requests through the throttle at around 1850 requests per second.

Features:
* Responds with an HTTP 429 to clients who exceed a preset per second rate throttle.
* Supports a cool off period enforced after a throttling event.
* Cool off period restarts if a throttling event occurs during the cool off.
* Cool off period remaining is efficiently retrieved to enable the Retry-After header to be sent back to the client.