package ru.mts.megogo.retrying;

import lombok.extern.slf4j.Slf4j;
import ru.mts.megogo.exception.RetryException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;


/**
 * Класс предназначен для подготовки стратигии перезапуска функции.
 */
@Slf4j
public class RetryStrategy<T> {
    private RetryCount retryCount;
    private Set<Class> exceptionsType = new HashSet<>();
    private Long waitAfterFail;
    private Long waitBeforeCall;
    private Set<Class> exceptionTypeForReturnNull = new HashSet<>();
    private List<Predicate<T>> predicatesForReturnNull = new ArrayList<>();
    private Long wait;
    private Callable<T> function;
    private List<Predicate<T>> predicates = new ArrayList<>();

    public static <T> RetryStrategy<T> newRetryStrategy(RetryCount retryCount) {
        RetryStrategy<T> retryStrategy = new RetryStrategy();
        return retryStrategy.addRetryCount(retryCount != null ? retryCount : new RetryCount(null));
    }

    public static <T> RetryStrategy<T> newRetryStrategy(Integer countRetry) {
        return RetryStrategy.<T>newRetryStrategy(new RetryCount(countRetry));
    }

    /**
     * Метод для добавления класса, который следит за кол-вом попыток
     *
     * @return RetryStrategy
     */
    public RetryStrategy<T> addRetryCount(RetryCount retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /**
     * Метод для добавления типов ошибок, при наличии которых, функция перезапуститься
     *
     * @return RetryStrategy
     */
    public RetryStrategy<T> retryIfException(Class<? extends Throwable> exception) {
        this.exceptionsType.add(exception);
        return this;
    }

    /**
     * Метод для добавления выполняемой функции
     *
     * @return RetryStrategy
     */
    public RetryStrategy<T> setFunction(Callable<T> function) {
        this.function = function;
        return this;
    }

    /**
     * Метод run() вернет null, если, выполнение функции завершится с ошибкой.
     * <p>
     * _Note_: Например выполнение запросов к серверу с непроверяемыми исходными данными.
     *
     * @return RetryStrategy
     */
    public RetryStrategy<T> returnNullIfError(Class<? extends Throwable> exception) {
        this.exceptionTypeForReturnNull.add(exception);
        return this;
    }

    /**
     * Метод run() вернет null, только если результат выполняемой функции будет соответсвовать хотя бы одному предикату.
     * <p>
     * _Note_: Например выполнение запросов к серверу с непроверяемыми исходными данными.
     *
     * @return RetryStrategy
     */
    public RetryStrategy<T> returnNullIfResultMatchPredicate(Predicate<T> predicate) {
        this.predicatesForReturnNull.add(predicate);
        return this;
    }

    /**
     * Метод для добавления предикатов (фукция, возвращающая boolean),
     * которые проверяют результат выполняемой функции.
     *
     * Выполняемая функция считается успешной, если все предикаты возвратили true.
     *
     * @return RetryStrategy
     */
    public RetryStrategy<T> successIf(Predicate<T> predicate) {
        this.predicates.add(predicate);
        return this;
    }

    /**
     * Метод для добавления времени ожидания между повторными вызовами функции после ошибки или проверки предиката
     *
     * @return RetryStrategy
     */
    public RetryStrategy<T> setTimeOutAfterFailCallFunction(long wait) {
        this.waitAfterFail = wait;
        return this;
    }

    /**
     * Метод для добавления времени ожидания до вызова функции при каждом повторе
     * <p><p/>
     * *NOTE*: Например: имитация скорости просмотра сайта человеком, когда получение страниц находится в цикле.
     *
     * @return RetryStrategy
     */
    public RetryStrategy<T> setTimeOutBeforeCallFunction(long wait) {
        this.waitBeforeCall = wait;
        return this;
    }

    /**
     * Основной метод для запуска и отслеживания функции
     *
     * Сценарий:
     * 1) Запускает фукцию,
     *      если без Exception см. п2
     *      инче см. п.3
     * 2) Проверяет результат, используя предикаты (при наличии)
     *      В случае успеха проверки, обновляет счетчик попыток из RetryCount и см. п.5,
     *      иначе см. п.4
     * 3) Проверяет присутвует ли ошибка в списке на обработку,
     *      если присутвтвует, то см. п.4
     *      иначе кидает RetryException c описание ошибки
     * 4) Проверяет кол-во попыток
     *      если > 0, то см. п.1
     *      инче Кидает RetryException
     * 5) Возвращает рузультат
     *
     * @return T
     */
    public T run() throws RetryException {
        try {
            if (retryCount.isFirstRetry()) {
                safeWait(waitBeforeCall);
            }
            T t = function != null ? function.call() : null;
            if (checkPredicateForReturnNull(t)) {
                return null;
            }
            return t != null && checkPredicateForSuccess(t)
                    ? success(t)
                    : failure(null);
        } catch (RetryException exception) {
            throw exception;
        } catch (Exception exception) {
            return exceptionProcessing(exception);
        }
    }

    /**
     * Метод уменьшает кол-во попыток и перезапускает выполнение фукции
     *
     * @return T
     */
    private T failure(Exception exception) throws RetryException {
        if (retryCount.isRetriesAvailable()) {
            safeWait(waitAfterFail);
            log.warn("Retry: attempt " + (retryCount.getOriginNumberRetries() - retryCount.getCurrentNumberRetries()));
            return run();
        }
        throw new RetryException(
                "The number of errors has been exhausted" + (exception == null ? ": result does not match predicate" : ""),
                retryCount,
                exception);
    }

    private void safeWait(Long wait) throws RetryException {
        if (wait == null || wait == 0) {
            return;
        }
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            throw new RetryException("Timer Error, ", retryCount, e);
        }
    }


    /**
     * Метод обновляет кол-во попыток и возвращает рещультат выполняемой фукции
     *
     * @return T
     */
    private T success(T t) {
        retryCount.rollbackRetries();
        return t;
    }

    private T exceptionProcessing(Exception exception) throws RetryException {
        for (Class typeException : this.exceptionTypeForReturnNull) {
            if (typeException.isInstance(exception)) {
                return null;
            }
        }
        for (Class typeException : this.exceptionsType) {
            if (typeException.isInstance(exception)) {
                return failure(exception);
            }
        }
        throw new RetryException("Unknown error", retryCount, exception);
    }

    private boolean checkPredicateForSuccess(T t) {
        return predicates.isEmpty() || predicates.stream().allMatch(predicate -> predicate.test(t));
    }

    private boolean checkPredicateForReturnNull(T t) {
        return !predicatesForReturnNull.isEmpty() && predicatesForReturnNull.stream().anyMatch(predicate -> predicate.test(t));
    }

}
