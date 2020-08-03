package com.stm.ivi.retrying;

/**
 * Класс предназначен для управления кол-вом попыток перезапуска фукции
 */
public class RetryCount {
    private Integer COUNT_RETRIES;
    private Integer ORIGINAL;

    public RetryCount(Integer count) {
        this.ORIGINAL = count != null ? count : 1;
        setCountRetries();
    }

    private void setCountRetries() {
        COUNT_RETRIES = ORIGINAL;
    }

    /**
     * Проверка на остаток кол-ва попыток
     *
     * @author boolean
     */
    public boolean isRetriesAvailable() {
        return --COUNT_RETRIES > 0;
    }

    /**
     * Обновляет кол-во попыток
     *
     * @author boolean
     */
    public void rollbackRetries() {
        setCountRetries();
    }

    public boolean isFirstRetry() {
        return ((COUNT_RETRIES != null) ? COUNT_RETRIES : 0) == ((ORIGINAL != null) ? ORIGINAL : 0);
    }

    /**
     * Создает стратегию перезапуска фукции,
     * используя указанное в данном классе кол-во попыток
     *
     * @author boolean
     */
    public <T> RetryStrategy<T> createStrategy() {
        return RetryStrategy.newRetryStrategy(this);
    }

    public Integer getCurrentNumberRetries() {
        return COUNT_RETRIES;
    }

    public Integer getOriginNumberRetries() {
        return ORIGINAL;
    }
}
