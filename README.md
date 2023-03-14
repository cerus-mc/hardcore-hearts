# hardcore-hearts

This plugin will trick your Minecraft client into displaying the hardcore hearts instead of the default ones.

<details>
<summary>Images</summary>
<p>Hardcore hearts:</p>
<img src="https://i.imgur.com/y13QXFB.png" alt="Hardcore Hearts">
<br>
<br>
<p>Default hearts:</p>
<img src="https://i.imgur.com/gAurB6e.png" alt="Hardcore Hearts">
</details>

## Requirements

(Checkout specific version branches for other versions)

- Spigot 1.19.4
- Java 17

## Incompatibilities

There are no incompatible plugins as far as I know.

## How it works

Short answer: The plugin listens for outgoing packets. When the JoinGame packet is sent to a player, the plugin modifies the packet by setting the "
hardcore" flag to "true".

Long answer:\
When I started to work on this project I thought the solution was pretty easy: Subscribe to the LoginEvent, inject a packet listener and modify the
JoinGame packet. Unfortunately there is no way to get the player's Netty channel during the login because it gets set right before the player receives
the JoinGame packet. While I was looking through the NMS code I stumbled upon a ITextFilter field. I suppose it's some sort of chat filter, but I have
no idea what its purpose is. I did notice however that the server calls this text filter right before it sends the JoinGame packet. The players Netty
channel is also set right before this. I just had to set my own text filter to receive the callback, and now I was able to inject my packet listener!