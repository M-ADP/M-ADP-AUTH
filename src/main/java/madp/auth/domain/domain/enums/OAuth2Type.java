package madp.auth.domain.domain.enums;

import lombok.RequiredArgsConstructor;
import madp.auth.domain.exception.UnsupportedOAuth2ProviderException;

import java.util.Arrays;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public enum OAuth2Type {
    GOOGLE("google"),
    GITHUB("github");

    private final String value;
    private static final Map<String, OAuth2Type> TYPE_MAP =
            Arrays.stream(values()).collect(toMap(type -> type.value, identity()));

    public static OAuth2Type of(String type) {
        OAuth2Type oAuth2Type = TYPE_MAP.get(type);
        if (oAuth2Type == null) {
            throw new UnsupportedOAuth2ProviderException(type);
        }
        return oAuth2Type;
    }
}