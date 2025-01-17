# This is a experimental fork!
The only purpose of this repository is to experiment with new appproaches to maybe achieve a better developer experience.
The official repository is linked [here](https://github.com/discord-jda/JDA).

[maven-central]: https://img.shields.io/maven-central/v/net.dv8tion/JDA?color=blue
[jitpack]: https://img.shields.io/badge/Snapshots-JitPack-blue
[download]: #download
[discord-invite]: https://discord.gg/0hMr4ce0tIl3SLv5
[migration]: https://jda.wiki/introduction/migration-v3-v4/
[jenkins]: https://ci.dv8tion.net/job/JDA5
[license]: https://github.com/discord-jda/JDA/tree/master/LICENSE
[faq]: https://jda.wiki/introduction/faq/
[docs]: https://docs.jda.wiki/index.html
[wiki]: https://jda.wiki/introduction/jda/
[troubleshooting]: https://jda.wiki/using-jda/troubleshooting/
[discord-shield]: https://discord.com/api/guilds/125227483518861312/widget.png
[faq-shield]: https://img.shields.io/badge/Wiki-FAQ-blue.svg
[docs-shield]: https://img.shields.io/badge/Wiki-Docs-blue.svg
[troubleshooting-shield]: https://img.shields.io/badge/Wiki-Troubleshooting-darkgreen.svg
[jenkins-shield]: https://img.shields.io/badge/Download-Jenkins-purple.svg
[license-shield]: https://img.shields.io/badge/License-Apache%202.0-white.svg
[migration-shield]: https://img.shields.io/badge/Wiki-Migrating%20from%20V3-darkgreen.svg

<img align="right" src="https://github.com/discord-jda/JDA/blob/assets/assets/readme/logo.png?raw=true" height="200" width="200">

[ ![maven-central][] ][download]
[ ![jitpack][] ](https://jitpack.io/#discord-jda/JDA)
[ ![jenkins-shield][] ][jenkins]
[ ![license-shield][] ][license]

[ ![discord-shield][] ][discord-invite]
[ ![faq-shield] ][faq]
[ ![docs-shield] ][docs]
[ ![troubleshooting-shield] ][troubleshooting]
[ ![migration-shield][] ][migration]


# JDA (Java Discord API)

JDA strives to provide a clean and full wrapping of the Discord REST API and its WebSocket-Events for Java.
This library is a helpful tool that provides the functionality to create a Discord bot in Java.

## Summary

1. [Introduction](#creating-the-jda-object)
2. [Sharding](#sharding-a-bot)
3. [Entity Lifetimes](#entity-lifetimes)
4. [Download](#download)
5. [Documentation](#documentation)
6. [Support](#getting-help)
7. [Extensions And Plugins](#third-party-recommendations)
8. [Contributing](#contributing-to-jda)
9. [Dependencies](#dependencies)
10. [Other Libraries](#related-projects)

## UserBots and SelfBots

Discord is currently prohibiting creation and usage of automated client accounts (AccountType.CLIENT).
We have officially dropped support for client login as of version **4.2.0**!
If you need a bot, use a bot account from the [Application Dashboard](https://discord.com/developers/applications).

[Read More](https://support.discord.com/hc/en-us/articles/115002192352-Automated-user-accounts-self-bots-)

## Creating the JDA Object

Creating the JDA Object is done via the JDABuilder class. After setting the token and other options via setters,
the JDA Object is then created by calling the `build()` method. When `build()` returns,
JDA might not have finished starting up. However, you can use `awaitReady()`
on the JDA object to ensure that the entire cache is loaded before proceeding.
Note that this method is blocking and will cause the thread to sleep until startup has completed.

**Example**:

```java
JDA jda = JDABuilder.createDefault("token").build();
```

### Configuration

Both the `JDABuilder` and the `DefaultShardManagerBuilder` allow a set of configurations to improve the experience.

**Example**:

```java
public static void main(String[] args) {
    JDABuilder builder = JDABuilder.createDefault(args[0]);
    
    // Disable parts of the cache
    builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
    // Enable the bulk delete event
    builder.setBulkDeleteSplittingEnabled(false);
    // Set activity (like "playing Something")
    builder.setActivity(Activity.watching("TV"));
    
    builder.build();
}
```

> See [JDABuilder](https://docs.jda.wiki/net/dv8tion/jda/api/JDABuilder.html)
  and [DefaultShardManagerBuilder](https://docs.jda.wiki/net/dv8tion/jda/api/sharding/DefaultShardManagerBuilder.html)

You can configure the memory usage by changing enabled `CacheFlags` on the `JDABuilder`.
Additionally, you can change the handling of the member/user cache by disabling **intents** or changing the **member cache policy**.
To learn more about intents and member loading/caching, read the [Gateway Intents Guide](https://jda.wiki/using-jda/gateway-intents-and-member-cache-policy/).

```java
public void configureMemoryUsage(JDABuilder builder) {
    // Disable cache for member activities (streaming/games/spotify)
    builder.disableCache(CacheFlag.ACTIVITY);

    // Only cache members who are either in a voice channel or owner of the guild
    builder.setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER));

    // Disable member chunking on startup
    builder.setChunkingFilter(ChunkingFilter.NONE);

    // Disable presence updates and typing events
    builder.disableIntents(GatewayIntent.GUILD_PRESENCE, GatewayIntent.GUILD_MESSAGE_TYPING);

    // Consider guilds with more than 50 members as "large". 
    // Large guilds will only provide online members in their setup and thus reduce bandwidth if chunking is disabled.
    builder.setLargeThreshold(50);
}
```

### Listening for Events

The event system in JDA is configured through a hierarchy of classes/interfaces.
We offer two implementations for the `IEventManager`:

- **InterfacedEventManager** which uses an `EventListener` interface and the `ListenerAdapter` abstract class
- **AnnotatedEventManager** which uses the `@SubscribeEvent` annotation which can be applied to methods

By default, the **InterfacedEventManager** is used.
Since you can create your own implementation of `IEventManager`, this is a very versatile and configurable system.
If the aforementioned implementations don't suit your use-case you can simply create a custom implementation and
configure it on the `JDABuilder` with `setEventManager(...)`.

#### Examples:

**Using EventListener**:

```java
public class ReadyListener implements EventListener {
    public static void main(String[] args) throws InterruptedException {
        // Note: It is important to register your ReadyListener before building
        JDA jda = JDABuilder.createDefault("token")
            .addEventListeners(new ReadyListener())
            .build();

        // optionally block until JDA is ready
        jda.awaitReady();
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            System.out.println("API is ready!");
        }
    }
}
```

**Using ListenerAdapter**:

```java
public class MessageListener extends ListenerAdapter {
    public static void main(String[] args) {
        JDA jda = JDABuilder.createDefault("token")
                .enableIntents(GatewayIntent.MESSAGE_CONTENT) // enables explicit access to message.getContentDisplay()
                .build();
        //You can also add event listeners to the already built JDA instance
        // Note that some events may not be received if the listener is added after calling build()
        // This includes events such as the ReadyEvent
        jda.addEventListener(new MessageListener());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf("[PM] %s: %s\n", event.getAuthor().getName(),
                                    event.getMessage().getContentDisplay());
        } else {
            System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(),
                        event.getTextChannel().getName(), event.getMember().getEffectiveName(),
                        event.getMessage().getContentDisplay());
        }
    }
}
```

**Slash-Commands**:

```java
public class Bot extends ListenerAdapter {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argument!");
            System.exit(1);
        }
        // args[0] would be the token (using an environment variable or config file is preferred for security)
        // We don't need any intents for this bot. Slash commands work without any intents!
        JDA jda = JDABuilder.createLight(args[0], Collections.emptyList())
            .addEventListeners(new Bot())
            .setActivity(Activity.playing("Type /ping"))
            .build();

        // Sets the global command list to the provided commands (removing all others)
        jda.updateCommands().addCommands(
            Commands.slash("ping", "Calculate ping of the bot"),
            Commands.slash("ban", "Ban a user from the server")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)) // only usable with ban permissions
                    .setGuildOnly(true) // Ban command only works inside a guild
                    .addOption(OptionType.USER, "user", "The user to ban", true) // required option of type user (target to ban)
                    .addOption(OptionType.STRING, "reason", "The ban reason") // optional reason
        ).queue();
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // make sure we handle the right command
        switch (event.getName()) {
            case "ping":
                long time = System.currentTimeMillis();
                event.reply("Pong!").setEphemeral(true) // reply or acknowledge
                     .flatMap(v ->
                          event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time) // then edit original
                     ).queue(); // Queue both reply and edit
                break;
            case "ban":
                // double check permissions, don't trust Discord on this!
                if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
                    event.reply("You cannot ban members! Nice try ;)").setEphemeral(true).queue();
                    break;
                }
                User target = event.getOption("user", OptionMapping::getUser);
                // optionally check for member information
                Member member = event.getOption("user", OptionMapping::getMember);
                if (!event.getMember().canInteract(member)) {
                    event.reply("You cannot ban this user.").setEphemeral(true).queue();
                    break;
                }
                // Before starting our ban request, tell the user we received the command
                // This sends a "Bot is thinking..." message which is later edited once we finished
                event.deferReply().queue();
                String reason = event.getOption("reason", OptionMapping::getAsString);
                AuditableRestAction<Void> action = event.getGuild().ban(target, 0); // Start building our ban request
                if (reason != null) // reason is optional
                    action = action.reason(reason); // set the reason for the ban in the audit logs and ban log
                action.queue(v -> {
                    // Edit the thinking message with our response on success
                    event.getHook().editOriginal("**" + target.getAsTag() + "** was banned by **" + event.getUser().getAsTag() + "**!").queue();
                }, error -> {
                    // Tell the user we encountered some error
                    event.getHook().editOriginal("Some error occurred, try again!").queue();
                    error.printStackTrace();
                });
                break;
            default:
                System.out.printf("Unknown command %s used by %#s%n", event.getName(), event.getUser());
        }
    }
}
```

### RestAction

Through [RestAction](https://docs.jda.wiki/net/dv8tion/jda/api/requests/RestAction.html) we provide request handling with
 
 - [callbacks](https://docs.jda.wiki/net/dv8tion/jda/api/requests/RestAction.html#queue%28java.util.function.Consumer%29)
 - [promises](https://docs.jda.wiki/net/dv8tion/jda/api/requests/RestAction.html#submit%28%29)
 - and [sync](https://docs.jda.wiki/net/dv8tion/jda/api/requests/RestAction.html#complete%28%29)

and it is up to the user to decide which pattern to utilize.
It can be combined with reactive libraries such as [reactor-core](https://github.com/reactor/reactor-core) due to being lazy.

The RestAction interface also supports a number of operators to avoid callback hell:

- [`map`](https://docs.jda.wiki/net/dv8tion/jda/api/requests/RestAction.html#map%28java.util.function.Function%29)
    Convert the result of the `RestAction` to a different value
- [`flatMap`](https://docs.jda.wiki/net/dv8tion/jda/api/requests/RestAction.html#flatMap%28java.util.function.Function%29)
    Chain another `RestAction` on the result
- [`delay`](https://docs.jda.wiki/net/dv8tion/jda/api/requests/RestAction.html#delay%28java.time.Duration%29)
    Delay the element of the previous step

**Example**:

```java
public RestAction<Void> selfDestruct(MessageChannel channel, String content) {
    return channel.sendMessage("The following message will destroy itself in 1 minute!")
        .delay(10, SECONDS, scheduler) // edit 10 seconds later
        .flatMap((it) -> it.editMessage(content))
        .delay(1, MINUTES, scheduler) // delete 1 minute later
        .flatMap(Message::delete);
}
```

### More Examples

We provide a small set of Examples in the [Example Directory](https://github.com/discord-jda/JDA/tree/master/src/examples/java).

## Sharding a Bot

When your bot joins over 2500 guilds, it is required to perform **Sharding**.
This means, your connection is split up into multiple **shards**, each only accessing a fraction of your total available guilds.
A shard can at most contain 2500 guilds when starting up the bot.

Each shard is assigned a **shard id** and **shard total** (usually shown as `id / total`), which uniquely identifies which guilds are accessible on that shard.
For instance, the first of 2 shards would be `0 / 2` and the second would be `1 / 2`.

If you want to use sharding with your bot, make use of the [DefaultShardManager](https://docs.jda.wiki/net/dv8tion/jda/api/sharding/DefaultShardManager.html) as seen in the example below.
This manager automatically assigns the right number of shards to your bot, so you do not need to do any math yourself.

Internally, this shard manager also handles the proper scaling of threads for connections and handles the login rate-limit (Identify Rate-Limit) to properly startup without issues.

If you do not want to use the shard manager, and instead manage sharding yourself, you can use [JDABuilder#useSharding](https://docs.jda.wiki/net/dv8tion/jda/api/JDABuilder.html#useSharding(int,int)) and [ConcurrentSessionController](https://docs.jda.wiki/net/dv8tion/jda/api/utils/ConcurrentSessionController.html).


### Example Sharding - Using DefaultShardManager

```java
public static void main(String[] args) {
    DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(args[0]);
    builder.addEventListeners(new MessageListener());
    builder.build();
}
```

## Entity Lifetimes

An **Entity** is the term used to describe types such as **GuildChannel**/**Message**/**User** and other entities that Discord provides.
Instances of these entities are created and deleted by JDA when Discord instructs it. This means the lifetime depends on signals provided by the Discord API which are used to create/update/delete entities.
This is done through Gateway Events known as "dispatches" that are handled by the JDA WebSocket handlers.
When Discord instructs JDA to delete entities, they are simply removed from the JDA cache and lose their references.
Once that happens, nothing in JDA interacts or updates the instances of those entities, and they become outdated.
Discord may instruct to delete these entities randomly for cache synchronization with the API.

**It is not recommended to store _any_ of these entities for a longer period of time!**
Instead of keeping (e.g.) a `User` instance in some field, an ID should be used. With the ID of a user,
you can use `getUserById(id)` to get and keep the user reference in a local variable (see below).

### Entity Updates

When an entity is updated through its manager, they will send a request to the Discord API which will update the state
of the entity. The success of this request **does not** imply the entity has been updated yet. All entities are updated
by the aforementioned **Gateway Events** which means you cannot rely on the cache being updated yet once the
execution of a RestAction has completed. Some requests rely on the cache being updated to correctly update the entity.
An example of this is updating roles of a member which overrides all roles of the member by sending a list of the
new set of roles. This is done by first checking the current cache, the roles the member has right now, and appending
or removing the requested roles. If the cache has not yet been updated by an event, this will result in unexpected behavior.

### Entity Deletion

Discord may request that a client (the JDA session) invalidates its entire cache. When this happens, JDA will remove all of its current entities and reconnect the session. This is signaled through the `SessionRecreateEvent`. When entities are removed from the JDA cache, your instance will keep stale entities in memory. This results in memory duplication, potential memory leaks, and outdated state. It is **highly recommended** to only keep references to entities by storing their **id** and using the respective `get...ById(id)` method when needed. Alternatively, keep the entity stored and make sure to replace it as soon as possible when the cache is replaced.

#### Example

```java
public class UserLogger extends ListenerAdapter {
    private final User user;
    
    public UserLogger(User user) {
        this.user = user;
    }
    
    private User getUser(JDA api) {
        // Acquire a reference to the User instance through the id
        User newUser = api.getUserById(this.user.getIdLong());
        if (newUser != null)
            this.user = newUser;
        return this.user;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        if (author.getIdLong() == this.user.getIdLong()) {
            // Update user from message instance (likely more up-to-date)
            this.user = author;
            // Print the message of the user
            System.out.println(author.getAsTag() + ": " + message.getContentDisplay());
        }
    }
    
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        JDA api = event.getJDA();
        User user = getUser(); // use getter to refresh user automatically on access
        user.openPrivateChannel().queue((channel) -> {
            // Send a private message to the user
            channel.sendMessageFormat("I have joined a new guild: **%s**", event.getGuild().getName()).queue();
        });
    }
}
```

## Download

[ ![maven-central][] ](https://mvnrepository.com/artifact/net.dv8tion/JDA/latest)
[ ![jitpack][] ](https://jitpack.io/#discord-jda/JDA)

Latest Release: [GitHub Release](https://github.com/discord-jda/JDA/releases/latest) <br>

Be sure to replace the **VERSION** key below with the one of the versions shown above! For snapshots, please use the instructions provided by [JitPack](https://jitpack.io/#discord-jda/JDA).

**Maven**
```xml
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>VERSION</version>
</dependency>
```

**Maven without Audio**
```xml
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>VERSION</version>
    <exclusions>
        <exclusion>
            <groupId>club.minnced</groupId>
            <artifactId>opus-java</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

**Gradle**
```gradle
repositories {
    mavenCentral()
}

dependencies {
    //Change 'implementation' to 'compile' in old Gradle versions
    implementation("net.dv8tion:JDA:VERSION")
}
```

**Gradle without Audio**
```gradle
dependencies {
    //Change 'implementation' to 'compile' in old Gradle versions
    implementation("net.dv8tion:JDA:VERSION") {
        exclude module: 'opus-java'
    }
}
```

The snapshot builds are only available via JitPack and require adding the JitPack resolver, you need to specify specific commits to access those builds.
Stable releases are published to [maven-central](https://mvnrepository.com/artifact/net.dv8tion/JDA).

If you do not need any opus de-/encoding done by JDA (voice receive/send with PCM) you can exclude `opus-java` entirely.
This can be done if you only send audio with an `AudioSendHandler` which only sends opus (`isOpus() = true`). (See [lavaplayer](https://github.com/sedmelluq/lavaplayer))

If you want to use a custom opus library you can provide the absolute path to `OpusLibrary.loadFrom(String)` before using
the audio API of JDA. This works without `opus-java-natives` as it only requires `opus-java-api`.
<br>_For this setup you should only exclude `opus-java-natives` as `opus-java-api` is a requirement for en-/decoding._

See [opus-java](https://github.com/discord-java/opus-java)

### Logging Framework - SLF4J

JDA is using [SLF4J](https://www.slf4j.org/) to log its messages.

That means you should add some SLF4J implementation to your build path in addition to JDA.
If no implementation is found, following message will be printed to the console on startup:
```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

JDA currently provides a fallback Logger in case that no SLF4J implementation is present.
We strongly recommend to use one though, as that can improve speed and allows you to customize the Logger as well as log to files

There is a guide for logback-classic available in our wiki: [Logging Setup](https://jda.wiki/setup/logging/)

## Documentation

Docs can be found on the [GitHub Pages][docs]
<br>We also have a wiki filled with information and troubleshooting guides at [jda.wiki][wiki]

### Annotations

We use a number of annotations to indicate future plans for implemented functionality such as new features of
the Discord API.

- [Incubating](https://github.com/discord-jda/JDA/blob/master/src/main/java/net/dv8tion/jda/annotations/Incubating.java)
    <br>This annotation is used to indicate that functionality may change in the future. Often used when a new feature is added.
- [ReplaceWith](https://github.com/discord-jda/JDA/blob/master/src/main/java/net/dv8tion/jda/annotations/ReplaceWith.java)
    <br>Paired with `@Deprecated` this is used to inform you how the new code-fragment is supposed to look once the hereby annotated functionality is removed.
- [ForRemoval](https://github.com/discord-jda/JDA/blob/master/src/main/java/net/dv8tion/jda/annotations/ForRemoval.java)
    <br>Paired with `@Deprecated` this indicates that we plan to entirely remove the hereby annotated functionality in the future.
- [DeprecatedSince](https://github.com/discord-jda/JDA/blob/master/src/main/java/net/dv8tion/jda/annotations/DeprecatedSince.java)
    <br>Paired with `@Deprecated` this specifies when a feature was marked as deprecated.

[Sources](https://github.com/discord-jda/JDA/tree/master/src/main/java/net/dv8tion/jda/annotations)

## Getting Help

For general troubleshooting you can visit our wiki [Troubleshooting][troubleshooting] and [FAQ][faq].
<br>If you need help, or just want to talk with the JDA or other Devs, you can join the [Official JDA Discord Guild][discord-invite].

Alternatively you can also join the [Unofficial Discord API Guild](https://discord.gg/discord-api).
Once you joined, you can find JDA-specific help in the `#java_jda` channel.

For guides and setup help you can also take a look at the [wiki](https://jda.wiki/)
<br>Especially interesting are the [Getting Started](https://jda.wiki/introduction/jda/)
and [Setup](https://jda.wiki/setup/intellij/) Pages.

## Third Party Recommendations

### [Lavaplayer](https://github.com/lavalink-devs/lavaplayer)

Created by [sedmelluq](https://github.com/sedmelluq) and now maintained by the [lavalink community](https://github.com/lavalink-devs)
<br>Lavaplayer is the most popular library used by Music Bots created in Java.
It is highly compatible with JDA and Discord4J and allows to play audio from
Youtube, Soundcloud, Twitch, Bandcamp and [more providers](https://github.com/lavalink-devs/lavaplayer#supported-formats).
<br>The library can easily be expanded to more services by implementing your own AudioSourceManager and registering it.

It is recommended to read the [Usage](https://github.com/lavalink-devs/lavaplayer#usage) section of Lavaplayer
to understand a proper implementation.
<br>Sedmelluq provided a demo in his repository which presents an example implementation for JDA:
https://github.com/lavalink-devs/lavaplayer/tree/master/demo-jda

### [Lavalink](https://github.com/lavalink-devs/Lavalink)

Created by [Freya Arbjerg](https://github.com/freyacodes) and now maintained by the [lavalink community](https://github.com/lavalink-devs).

Lavalink is a popular standalone audio sending node based on Lavaplayer. Lavalink was built with scalability in mind,
and allows streaming music via many servers. It supports most of Lavaplayer's features.

Lavalink is used by many large bots, as well as bot developers who can not use a Java library like Lavaplayer.
If you plan on serving music on a smaller scale with JDA it is often preferable to just use Lavaplayer directly
as it is easier.

[Lavalink-Client](https://github.com/FredBoat/Lavalink-Client) is the official Lavalink client for JDA.


### [jda-nas](https://github.com/sedmelluq/jda-nas) and [udpqueue](https://github.com/MinnDevelopment/udpqueue.rs)

Created and maintained by [sedmelluq](https://github.com/sedmelluq) and extended by [MinnDevelopment](https://github.com/MinnDevelopment)
<br>Provides a native implementation for the JDA Audio Send-System to avoid GC pauses.

Note that this send system creates an extra UDP-Client which causes audio receive to no longer function properly,
since Discord identifies the sending UDP-Client as the receiver.

```java
JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN)
    .setAudioSendFactory(new NativeAudioSendFactory());
```

### [jda-ktx](https://github.com/MinnDevelopment/jda-ktx)

Created and maintained by [MinnDevelopment](https://github.com/MinnDevelopment).
<br>Provides [Kotlin](https://kotlinlang.org/) extensions for **RestAction** and events that provide a more idiomatic Kotlin experience.

```kotlin
fun main() {
    val jda = light(BOT_TOKEN)
    
    jda.onCommand("ping") { event ->
        val time = measureTime {
            event.reply("Pong!").await() // suspending
        }.inWholeMilliseconds

        event.hook.editOriginal("Pong: $time ms").queue()
    }
}
```

There is a number of examples available in the [README](https://github.com/MinnDevelopment/jda-ktx/#jda-ktx).

------

More can be found in our github organization: [JDA-Applications](https://github.com/JDA-Applications)

## Contributing to JDA

If you want to contribute to JDA, make sure to base your branch off of our **master** branch (or a feature-branch)
and create your PR into that **same** branch.

More information can be found at the wiki page [Contributing](https://jda.wiki/contributing/contributing/).

## Versioning and Deprecation Policy

Since the Discord API is in itself a moving standard, the stability is never guaranteed. For this reason, JDA does not follow the common semver versioning strategy.

The JDA version is structured with a looser definition, where the version change indicates the significance of changes.
For instance, using `5.1.2` as a baseline:

- A change to the major like `6.0.0` indicates that a lot of code has to be adjusted due to major changes to the interfaces. A change like this always comes with a full migration guide like [Migrating from 4.X to 5.X](https://jda.wiki/introduction/migration-v4-v5/).
- A change to the minor like `5.2.0` indicates some code may need to be adjusted due to the removal or change of interfaces. You can usually find the necessary changes in the release documentation.
- A change to the patch like `5.1.3` indicates bug fixes and new feature additions that are backwards compatible.

If a feature is marked as deprecated, it usually also indicates an alternative. For instance:

```java
@Deprecated
@DeprecatedSince("5.1.2")
@ForRemoval(deadline="5.2.0")
@ReplaceWith("setFoo(foo)")
public void changeFoo(Foo foo) { ... }
```

The method `changeFoo` was deprecated in release `5.1.2` and is going to be removed in `5.2.0`. Your change should replace all usage of `changeFoo(foo)` with `setFoo(foo)`.

Sometimes, a feature might be removed without a replacement. This will be clearly explained in the documentation.


## Dependencies:

This project requires **Java 8+**.<br>
All dependencies are managed automatically by Gradle.
 * NV WebSocket Client
   * Version: **2.14**
   * [Github](https://github.com/TakahikoKawasaki/nv-websocket-client)
 * OkHttp
   * Version: **4.10.0**
   * [Github](https://github.com/square/okhttp)
 * Apache Commons Collections4
   * Version: **4.4**
   * [Website](https://commons.apache.org/proper/commons-collections)
 * jackson
   * Version: **2.14.1**
   * [Github](https://github.com/FasterXML/jackson)
 * Trove4j
   * Version: **3.0.3**
   * [BitBucket](https://bitbucket.org/trove4j/trove)
 * slf4j-api
   * Version: **1.7.36**
   * [Website](https://www.slf4j.org/)
 * opus-java (optional)
   * Version: **1.1.1**
   * [GitHub](https://github.com/discord-java/opus-java)

## Related Projects

- [Discord4J](https://github.com/Discord4J/Discord4J)
- [Discord.NET](https://github.com/discord-net/Discord.Net)
- [discord.py](https://github.com/Rapptz/discord.py)
- [serenity](https://github.com/serenity-rs/serenity)

**See also:** [Discord API Community Libraries](https://github.com/apacheli/discord-api-libs)
