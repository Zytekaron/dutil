package tk.zytekaron.dutil;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import tk.zytekaron.dutil.events.EventListener;
import tk.zytekaron.dutil.menu.paginator.Paginator;

public class Main {
    private static JDA jda;
    private static EventListener listener = new EventListener();
    
    public static void main(String[] args) {
        jda.addEventListener(listener);
        listener.add(MessageReactionAddEvent.class, event -> {
            System.out.println("MessageReactionAddEvent fire");
        });
    }
}