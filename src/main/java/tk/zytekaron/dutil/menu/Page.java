package tk.zytekaron.dutil.menu;

import net.dv8tion.jda.api.entities.MessageEmbed;

public class Page {
    private PageType type;
    private String text;
    private MessageEmbed embed;
    
    public Page(String text) {
        this.type = PageType.TEXT;
        this.text = text;
    }
    
    public Page(MessageEmbed embed) {
        this.type = PageType.EMBED;
        this.embed = embed;
    }
    
    public PageType getType() {
        return type;
    }
    
    public String getText() {
        return text;
    }
    
    public MessageEmbed getEmbed() {
        return embed;
    }
}