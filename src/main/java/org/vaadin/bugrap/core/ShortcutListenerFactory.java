package org.vaadin.bugrap.core;

import com.vaadin.event.ShortcutListener;

import java.util.function.BiConsumer;

/**
 * @author oladeji
 */
public class ShortcutListenerFactory {

  public static ShortcutListener create(String caption, int keyCode, int[] modifiers, BiConsumer<Object, Object> consumer) {
    return new ShortcutListener(caption, keyCode, modifiers) {
      @Override
      public void handleAction(Object sender, Object target) {
        consumer.accept(sender, target);
      }
    };
  }
}
