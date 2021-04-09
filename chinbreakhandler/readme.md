# Chin break handler

This plugin will schedule breaks for you. 
Your plugin decides when it's a good time for taking the scheduled break. 
For there is a NMZ plugin running and there is a break scheduled. 
The NMZ plugin will wait for the dream to end before giving the green light for taking the break.
From that point on the break handler plugin will handle the logout and login handelings.
This plugin can easily be implemented into any plugin as long as your plugin is able to recover itself from a logout / login handling.
Implementing it into your plugin is very easy and it'll only take 10 - 20 lines of code.

Your build script should contain this plugin as an compile time only dependency. Build this plugin and run `publishToMavenLocal`.

plugin.gradle.kts
```kotlin
compileOnly(group = "com.owain.externals", name = "chinbreakhandler", version = "0.0.2+")
```

The brains of the operation is the ChinBreakHandler file, this file handles communication between your plugin and the break handler plugin, you need to inject this into your plugin.

Plugin.java
```java
@Inject
private ChinBreakHandler chinBreakHandler;
```

After this let the break handler know about the existence of your plugin. And of course let it know if your plugin stops.

```java
protected void startUp()
{
    chinBreakHandler.registerPlugin(this);
}
```

```java
protected void shutDown()
{
    chinBreakHandler.unregisterPlugin(this);
}
```

This way people can configure the threshold and break timings in the Chin break handler plugin panel. 
In some cases you don't want users to be able to configure the timings, for example in my giant seaweed plugin an break should be taken when the plugin is done harvesting and planting the giant seaweed.
The timing and the duration are defined by the plugin in that case.
If you need this you can use the following register function

```java
protected void startUp()
{
    chinBreakHandler.registerPlugin(this, false);
}
```

Next up, you should let the plugin know when an user starts and stops your plugin so the break handler knows it should schedule a break.
Personally i use a hotkey bind for starting my plugins which calls these functions. Basically call `chinBreakHandler.startPlugin(this)` when your plugin is started and call `chinBreakHandler.stopPlugin(this);`  when it is stopped.

```java
private void startState()
{
    chinBreakHandler.startPlugin(this);
    enabled = true;
}

private void stopState()
{
    chinBreakHandler.stopPlugin(this);
    enabled = false;
}
```

When a break is active your plugin shouldn't do anything so you should block your plugin main loop. Most of the times the main loop is based on the gametick event. This is needed because a user can choose between 'AFK' and logout as a break.

```java
@Subscribe
private void onGameTick(GameTick gameTick)
{
    if (!enabled || chinBreakHandler.isBreakActive(this))
    {
        return;
    }
}
```

When a break is scheduled it'll wait for the plugin to let the break handler when it's a good time. 
For example in my NMZ plugin i only allow breaks when the user isn't in a dream. interrupting the dream would be a bad thing.
You can use `chinBreakHandler.shouldBreak(this)` to check if there is a break currently pending. To start the break you can call `chinBreakHandler.startBreak(this)`

```java
if (chinBreakHandler.shouldBreak(this))
{
    chinBreakHandler.startBreak(this);
}
```

This will automatically take a break with a duration based on the user their settings. If you registered the plugin with the extra `false` overload to make it not configurable by the user you'll need pass a duration the the startBreak function.

```java
chinBreakHandler.startBreak(this, Instant.now().plus(15, ChronoUnit.MINUTES));
```

And that's everything you'll need to do to implement this into your plugin!


If you just simply want to logout of the game, for example when you hit an error state you can use the following. (This also works when your plugin isn't registered)
```java
chinBreakHandler.logoutNow(this);
```



## Complete code

Configurable by the user
```java
@Inject
private ChinBreakHandler chinBreakHandler;

protected void startUp()
{
    chinBreakHandler.registerPlugin(this);
}

protected void shutDown()
{
    chinBreakHandler.unregisterPlugin(this);
}

private void startState()
{
    chinBreakHandler.startPlugin(this);
    enabled = true;
}

private void stopState()
{
    chinBreakHandler.stopPlugin(this);
    enabled = false;
}

@Subscribe
private void onGameTick(GameTick gameTick)
{
    if (!enabled || chinBreakHandler.isBreakActive(this))
    {
        return;
    }

    if (chinBreakHandler.shouldBreak(this))
    {
        chinBreakHandler.startBreak(this);
    }
}
```

Not configurable by the user
```java
@Inject
private ChinBreakHandler chinBreakHandler;

protected void startUp()
{
    chinBreakHandler.registerPlugin(this, false);
}

protected void shutDown()
{
    chinBreakHandler.unregisterPlugin(this);
}

private void startState()
{
    chinBreakHandler.startPlugin(this);
    enabled = true;
}

private void stopState()
{
    chinBreakHandler.stopPlugin(this);
    enabled = false;
}

@Subscribe
private void onGameTick(GameTick gameTick)
{
    if (!enabled || chinBreakHandler.isBreakActive(this))
    {
        return;
    }

    chinBreakHandler.startBreak(this, Instant.now().plus(15, ChronoUnit.MINUTES));
}
```

## Making Chin break handler optional

[Soxs](https://github.com/Soxs "Soxs Github") made a util to make the chin break handler fully optional. This way your users don't need my repository added in their OpenOSRS. When they do and have the chin break handler installed your plugin will show up and users can make use of the functionality. When they don't have the chin breka handler installed your plugin will still work like expected.

[ReflectBreakHandler.java](https://gist.github.com/Soxs/50c3e49186a60f79b07ee976879a6c33)

Simply add the code in the gist above to your project and change the following from the code examples above.

From:

```java
@Inject
private ChinBreakHandler chinBreakHandler;
```

To:

```java
@Inject
private ReflectBreakHandler chinBreakHandler;
```