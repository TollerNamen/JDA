package net.dv8tion.jda.api.hooks;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class AnonymousListenerAdapter implements EventListener
{
    public static final HashMap<String, ButtonInteractionHandler> buttonInteractionHandlers = new HashMap<>();
    public static final HashMap<String, SlashCommandInteractionHandler> slashCommandInteractionHandlers = new HashMap<>();
    public static final HashMap<String, StringSelectInteractionHandler> stringSelectInteractionHandlers = new HashMap<>();
    @Override
    public void onEvent(@Nonnull GenericEvent event)
    {
        if (event instanceof SlashCommandInteractionEvent)
        {
            SlashCommandInteractionHandler handler = slashCommandInteractionHandlers.get(((SlashCommandInteractionEvent) event).getName());
            if (handler != null)
            {
                handler.handle((SlashCommandInteractionEvent) event);
            }
        }
        else if (event instanceof ButtonInteractionEvent)
        {
            ButtonInteractionHandler handler = buttonInteractionHandlers.get(((ButtonInteractionEvent) event).getButton().getId());
            if (handler != null)
            {
                handler.handle((ButtonInteractionEvent) event);
            }
        }
        else if (event instanceof StringSelectInteractionEvent)
        {
            StringSelectInteractionHandler handler = stringSelectInteractionHandlers.get(((StringSelectInteractionEvent) event).getSelectMenu().getId());
            if (handler != null)
            {
                handler.handle((StringSelectInteractionEvent) event);
            }
        }
    }
    public interface ButtonInteractionHandler
    {
        void handle(ButtonInteractionEvent buttonInteractionEvent);
    }
    public interface SlashCommandInteractionHandler
    {
        void handle(SlashCommandInteractionEvent event);
    }
    public interface StringSelectInteractionHandler
    {
        void handle(StringSelectInteractionEvent event);
    }
}
