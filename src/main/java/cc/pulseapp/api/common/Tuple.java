package cc.pulseapp.api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A simple tuple utility.
 *
 * @author Braydon
 */
@AllArgsConstructor @Setter @Getter
public final class Tuple<L, R> {
    private L left;
    private R right;
}