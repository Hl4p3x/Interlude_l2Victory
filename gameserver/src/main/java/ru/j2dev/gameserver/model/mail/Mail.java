package ru.j2dev.gameserver.model.mail;

import ru.j2dev.commons.dao.JdbcEntity;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.gameserver.dao.MailDAO;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.HashSet;
import java.util.Set;

public class Mail implements JdbcEntity, Comparable<Mail> {
    public static final int DELETED = 0;
    public static final int READED = 1;
    public static final int REJECTED = 2;
    private static final long serialVersionUID = -8704970972611917153L;
    private static final MailDAO _mailDAO = MailDAO.getInstance();
    private final Set<ItemInstance> attachments;
    private int messageId;
    private int senderId;
    private String senderName;
    private int receiverId;
    private String receiverName;
    private int expireTime;
    private String topic;
    private String body;
    private long price;
    private SenderType _type;
    private boolean isUnread;
    private JdbcEntityState _state;

    public Mail() {
        _type = SenderType.NORMAL;
        attachments = new HashSet<>();
        _state = JdbcEntityState.CREATED;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(final int messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(final int senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(final String senderName) {
        this.senderName = senderName;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(final int receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(final String receiverName) {
        this.receiverName = receiverName;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(final int expireTime) {
        this.expireTime = expireTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public boolean isPayOnDelivery() {
        return price > 0L;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(final long price) {
        this.price = price;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(final boolean isUnread) {
        this.isUnread = isUnread;
    }

    public Set<ItemInstance> getAttachments() {
        return attachments;
    }

    public void addAttachment(final ItemInstance item) {
        attachments.add(item);
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || (o != null && o.getClass() == getClass() && ((Mail) o).getMessageId() == getMessageId());
    }

    @Override
    public JdbcEntityState getJdbcState() {
        return _state;
    }

    @Override
    public void setJdbcState(final JdbcEntityState state) {
        _state = state;
    }

    @Override
    public void save() {
        _mailDAO.save(this);
    }

    @Override
    public void update() {
        _mailDAO.update(this);
    }

    @Override
    public void delete() {
        _mailDAO.delete(this);
    }

    public Mail reject() {
        final Mail mail = new Mail();
        mail.setSenderId(getReceiverId());
        mail.setSenderName(getReceiverName());
        mail.setReceiverId(getSenderId());
        mail.setReceiverName(getSenderName());
        mail.setTopic(getTopic());
        mail.setBody(getBody());
        synchronized (getAttachments()) {
            getAttachments().forEach(mail::addAttachment);
            getAttachments().clear();
        }
        mail.setType(SenderType.NEWS_INFORMER);
        mail.setUnread(true);
        return mail;
    }

    public Mail reply() {
        final Mail mail = new Mail();
        mail.setSenderId(getReceiverId());
        mail.setSenderName(getReceiverName());
        mail.setReceiverId(getSenderId());
        mail.setReceiverName(getSenderName());
        mail.setTopic("[Re]" + getTopic());
        mail.setBody(getBody());
        mail.setType(SenderType.NEWS_INFORMER);
        mail.setUnread(true);
        return mail;
    }

    @Override
    public int compareTo(final Mail o) {
        return o.getMessageId() - getMessageId();
    }

    public SenderType getType() {
        return _type;
    }

    public void setType(final SenderType type) {
        _type = type;
    }

    public enum SenderType {
        NORMAL,
        NEWS_INFORMER,
        NONE,
        BIRTHDAY;

        public static final SenderType[] VALUES = values();

    }
}
