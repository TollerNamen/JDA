import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.AnonymousListenerAdapter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import javax.annotation.Nonnull;

public class LambdaEventListenerExample extends ListenerAdapter
{
    public static void main(@Nonnull String[] args)
    {

        if (args.length == 0)
        {
            System.err.println("Unable to start without token!");
            System.exit(1);
        }
        String token = args[0];

        JDA jda = JDABuilder
            .createDefault(token)
            .setActivity(Activity.listening("interactions"))
            .setStatus(OnlineStatus.ONLINE)
            .setAutoReconnect(true)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.NONE)
            .enableIntents(GatewayIntent.GUILD_MESSAGES)
            .addEventListeners(new AnonymousListenerAdapter())
            .addEventListeners(new LambdaEventListenerExample())
            .build();
    }
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event)
    {
        if (event.getMessage().getContentStripped().startsWith("!test"))
        {
            Button button = Button.success("lambda", "Let Lambda handle this!");

            button.setOnInteractionEvent(buttonInteractionEvent ->
                    buttonInteractionEvent.reply("Successful LambdaEventHandling!").queue());

            event.getChannel().sendMessage("Hey this is a Test!").addActionRow(button).queue();
        }
    }
}
