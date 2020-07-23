package com.zytekaron.dutil.menu;

import com.zytekaron.dutil.events.EventListener;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Menu {
    private Message message;
    private EventListener listener;
    private List<Page> pages = new ArrayList<>();
    
    public void start(Message message) {
        this.message = message;
        start();
    }
    
    public abstract void start();
    
    public abstract void stop();
    
    public void addPage(Page page) {
        pages.add(page);
    }
    
    public void addPages(Page... pgs) {
        pages.addAll(Arrays.asList(pgs));
    }
    
    public Message getMessage() {
        return message;
    }
    
    public void setMessage(Message message) {
        this.message = message;
    }
    
    public EventListener getListener() {
        return listener;
    }
    
    public void setListener(EventListener listener) {
        this.listener = listener;
    }
    
    public Page getPage(int index) {
        return pages.get(index);
    }
    
    public List<Page> getPages() {
        return pages;
    }
}