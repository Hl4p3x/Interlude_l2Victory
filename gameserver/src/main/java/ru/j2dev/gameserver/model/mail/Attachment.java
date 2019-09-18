package ru.j2dev.gameserver.model.mail;

import ru.j2dev.gameserver.model.items.ItemInstance;

public class Attachment {
    private int messageId;
    private ItemInstance item;
    private Mail mail;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(final int messageId) {
        this.messageId = messageId;
    }

    public ItemInstance getItem() {
        return item;
    }

    public void setItem(final ItemInstance item) {
        this.item = item;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(final Mail mail) {
        this.mail = mail;
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == getClass() && ((Attachment) o).getItem() == getItem());
    }
}
