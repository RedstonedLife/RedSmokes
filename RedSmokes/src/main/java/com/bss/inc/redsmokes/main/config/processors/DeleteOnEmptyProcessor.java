package com.bss.inc.redsmokes.main.config.processors;

public class DeleteOnEmptyProcessor implements Processor<Object> {
    @Override
    public void process(final Object value, final ConfigurationNode destination) {
        if (value == null) {
            return;
        }
        try {
            if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
                destination.set(null);
            } else if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                destination.set(null);
            } else if (value instanceof CharSequence && ((CharSequence) value).length() == 0) {
                destination.set(null);
            }
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }
}
