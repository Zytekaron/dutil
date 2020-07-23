package com.zytekaron.dutil;

import com.zytekaron.dutil.events.EventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

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