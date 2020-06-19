package tk.zytekaron.dutil.menu.paginator;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.jetbrains.annotations.NotNull;
import tk.zytekaron.dutil.events.EventListener;
import tk.zytekaron.dutil.events.SubscribedListener;
import tk.zytekaron.dutil.menu.Menu;
import tk.zytekaron.dutil.menu.Page;
import tk.zytekaron.dutil.menu.PageType;
import tk.zytekaron.jvar.Maths;
import tk.zytekaron.jvar.timer.RunnableTimer;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Paginator extends Menu {
    public static final int INFO = -1;
    private boolean enabled = false;
    private boolean updateOnRemove = true;
    private long authorID;
    private Predicate<MessageReactionAddEvent> addPredicate = event -> event.getUserIdLong() == authorID && event.getMessageIdLong() == getMessage().getIdLong();
    private Predicate<MessageReactionRemoveEvent> removePredicate = event -> updateOnRemove && event.getUserIdLong() == authorID && event.getMessageIdLong() == getMessage().getIdLong();
    private Predicate<MessageReceivedEvent> messagePredicate = event -> event.getAuthor().getIdLong() == authorID && event.getChannel().getIdLong() == getMessage().getChannel().getIdLong();
    private SubscribedListener addListener;
    private SubscribedListener removeListener;
    private SubscribedListener messageListener;
    private String leftEmoji = "\u2B05";
    private String rightEmoji = "\u27A1";
    private String stopEmoji = "\u23F9";
    private String infoEmoji = "\u2139";
    private Page infoPage;
    private long timeoutDelay;
    private TimeUnit timeoutUnit;
    private int currentPage = 0;
    private int lastPage = 0;
    
    public void start() {
        Message message = getMessage();
        EventListener listener = getListener();
        if (message == null) {
            throw new PaginatorException("I don't like that message (null)");
        }
        message.addReaction(leftEmoji).queue();
        message.addReaction(stopEmoji).queue();
        message.addReaction(rightEmoji).queue();
        if (infoPage != null) {
            message.addReaction(infoEmoji).queue();
        }
        enabled = true;
        if (timeoutUnit != null && timeoutDelay > 0) {
            new RunnableTimer(timeoutDelay, timeoutUnit, this::stop).run();
        }
        addListener = listener.add(MessageReactionAddEvent.class, addPredicate, this::handleEvent);
        removeListener = listener.add(MessageReactionRemoveEvent.class, removePredicate, this::handleEvent);
        messageListener = listener.add(MessageReceivedEvent.class, messagePredicate, this::handleEvent);
    }
    
    public void stop() {
        enabled = false;
        addListener.unsubscribe();
        removeListener.unsubscribe();
        messageListener.unsubscribe();
        getMessage().clearReactions().queue();
    }
    
    public void navigateLeft() {
        navigateTo(currentPage - 1);
    }
    
    public void navigateRight() {
        navigateTo(currentPage + 1);
    }
    
    private void navigateTo(int newPage) {
        if (!enabled) return;
        if (newPage == INFO) {
            if (currentPage == INFO) {
                currentPage = lastPage;
            } else {
                lastPage = currentPage;
                currentPage = INFO;
            }
        } else {
            lastPage = currentPage;
            currentPage = Maths.constrain(newPage, 0, getPages().size() - 1);
        }
        
        Page page;
        if (currentPage == INFO) {
            page = infoPage;
        } else {
            page = getPage(currentPage);
        }
        if (page.getType() == PageType.TEXT) {
            String text = page.getText();
            getMessage().editMessage(text).queue();
        } else {
            MessageEmbed embed = page.getEmbed();
            getMessage().editMessage(embed).queue();
        }
    }
    
    private void handleReaction(@NotNull User user, MessageReaction reaction) {
        if (user.getIdLong() != authorID) return;
        MessageReaction.ReactionEmote emote = reaction.getReactionEmote();
        String id = "";
        if (!emote.isEmoji()) {
            id = emote.getId();
        }
        String emoji = emote.getEmoji();
        if (id.equals(leftEmoji) || emoji.equals(leftEmoji)) {
            navigateLeft();
        }
        if (id.equals(stopEmoji) || emoji.equals(stopEmoji)) {
            stop();
        }
        if (id.equals(rightEmoji) || emoji.equals(rightEmoji)) {
            navigateRight();
        }
        if (id.equals(infoEmoji) || emoji.equals(infoEmoji)) {
            if (infoPage != null) {
                navigateTo(INFO);
            }
        }
    }
    
    private void handleEvent(@NotNull MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null) return;
        MessageReaction reaction = event.getReaction();
        handleReaction(user, reaction);
        if (!updateOnRemove) {
            System.out.println("run");
            Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
            message.removeReaction(reaction.getReactionEmote().getName(), event.getUser()).queue();
        }
    }
    
    private void handleEvent(@NotNull MessageReactionRemoveEvent event) {
        User user = event.getUser();
        if (user == null) return;
        handleReaction(user, event.getReaction());
    }
    
    private void handleEvent(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        try {
            int page = Integer.parseInt(content) - 1;
            if (page >= 0 && page < getPages().size()) {
                message.delete().queue();
                navigateTo(page);
            }
        } catch (NumberFormatException e) {
            Pattern info = Pattern.compile("^i(nfo)?$", Pattern.CASE_INSENSITIVE);
            Pattern left = Pattern.compile("^l(eft)?$", Pattern.CASE_INSENSITIVE);
            Pattern right = Pattern.compile("^r(ight)?$", Pattern.CASE_INSENSITIVE);
            if (info.matcher(content).matches()) {
                navigateTo(INFO);
                message.delete().queue();
            }
            if (left.matcher(content).matches()) {
                navigateLeft();
                message.delete().queue();
            }
            if (right.matcher(content).matches()) {
                navigateRight();
                message.delete().queue();
            }
        }
    }
    
    public void setUpdateOnRemove(boolean updateOnRemove) {
        this.updateOnRemove = updateOnRemove;
    }
    
    public void setAddPredicate(Predicate<MessageReactionAddEvent> addPredicate) {
        this.addPredicate = addPredicate;
    }
    
    public void setRemovePredicate(Predicate<MessageReactionRemoveEvent> removePredicate) {
        this.removePredicate = removePredicate;
    }
    
    public void setMessagePredicate(Predicate<MessageReceivedEvent> messagePredicate) {
        this.messagePredicate = messagePredicate;
    }
    
    public void setLeftEmoji(String leftEmoji) {
        this.leftEmoji = leftEmoji;
    }
    
    public void setRightEmoji(String rightEmoji) {
        this.rightEmoji = rightEmoji;
    }
    
    public void setStopEmoji(String stopEmoji) {
        this.stopEmoji = stopEmoji;
    }
    
    public void setInfoEmoji(String infoEmoji) {
        this.infoEmoji = infoEmoji;
    }
    
    public void setAuthor(@NotNull User author) {
        this.authorID = author.getIdLong();
    }
    
    public void setAuthor(long authorID) {
        this.authorID = authorID;
    }
    
    public void setInfoPage(Page infoPage) {
        this.infoPage = infoPage;
    }
    
    public void setTimeout(long timeoutDelay, TimeUnit timeoutUnit) {
        this.timeoutDelay = timeoutDelay;
        this.timeoutUnit = timeoutUnit;
    }
    
    public static class Builder {
        private Paginator paginator = new Paginator();
        
        public Builder setMessage(Message message) {
            paginator.setMessage(message);
            return this;
        }
        
        public Builder setListener(EventListener listener) {
            paginator.setListener(listener);
            return this;
        }
        
        public Builder setUpdateOnRemove(boolean updateOnRemove) {
            paginator.setUpdateOnRemove(updateOnRemove);
            return this;
        }
        
        public Builder setAddPredicate(Predicate<MessageReactionAddEvent> predicate) {
            paginator.setAddPredicate(predicate);
            return this;
        }
        
        public Builder setRemovePredicate(Predicate<MessageReactionRemoveEvent> predicate) {
            paginator.setRemovePredicate(predicate);
            return this;
        }
        
        public Builder setMessagePredicate(Predicate<MessageReceivedEvent> messagePredicate) {
            paginator.setMessagePredicate(messagePredicate);
            return this;
        }
        
        public Builder addPage(Page page) {
            paginator.addPage(page);
            return this;
        }
        
        public Builder addPages(Page... pages) {
            paginator.addPages(pages);
            return this;
        }
        
        public Builder setLeftEmoji(String leftEmoji) {
            paginator.setLeftEmoji(leftEmoji);
            return this;
        }
        
        public Builder setRightEmoji(String rightEmoji) {
            paginator.setRightEmoji(rightEmoji);
            return this;
        }
        
        public Builder setStopEmoji(String stopEmoji) {
            paginator.setStopEmoji(stopEmoji);
            return this;
        }
        
        public Builder setInfoEmoji(String infoEmoji) {
            paginator.setInfoEmoji(infoEmoji);
            return this;
        }
        
        public Builder setAuthor(User author) {
            paginator.setAuthor(author);
            return this;
        }
        
        public Builder setAuthor(long authorID) {
            paginator.setAuthor(authorID);
            return this;
        }
        
        public Builder setInfoPage(Page page) {
            paginator.setInfoPage(page);
            return this;
        }
        
        public Builder setTimeout(long timeoutDelay, TimeUnit timeoutUnit) {
            paginator.setTimeout(timeoutDelay, timeoutUnit);
            return this;
        }
        
        public Paginator build() {
            return paginator;
        }
    }
}