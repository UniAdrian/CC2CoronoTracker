package de.uni.cc2coronotracker.helper;

/**
 * Adopted from <url>https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150</url>
 * @param <Out> The content stored in the event
 */
public class Event<Out> {

    private final Out content;
    private boolean hasBeenHandled = false;

    public Event(Out content) {
        this.content = content;
    }


    public boolean isHandled() {
        return hasBeenHandled;
    }

    /**
     * Returns the content and prevents its use again.
     */
    public Out getContentIfNotHandled() {
        if (hasBeenHandled)
            return null;

        hasBeenHandled = true;
        return content;
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public Out peekContent() {
        return content;
    }
}
