package org.vaadin.bugrap.core;

import com.vaadin.event.ShortcutListener;

import java.util.function.BiConsumer;

/**
 * This class exist only because Kotlin compiler does not interpret varargs as array,
 * making it hard to target methods with parameter types (T, T...)
 *
 * @author oladeji
 */
public class ShortcutListenerFactory {

  public static ShortcutListener newShortcutListener(String caption, int keyCode, int[] modifiers,
                                                     BiConsumer<Object, Object> consumer) {

    return new ShortcutListener(caption, keyCode, modifiers) {
      @Override
      public void handleAction(Object sender, Object target) {
        consumer.accept(sender, target);
      }
    };
  }
}
