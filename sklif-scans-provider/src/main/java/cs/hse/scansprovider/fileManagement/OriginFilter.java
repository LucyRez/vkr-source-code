package cs.hse.scansprovider.fileManagement;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;


@NoArgsConstructor
public class OriginFilter {

    Logger logger = LoggerFactory.getLogger(OriginFilter.class);

    private final List<String> permittedOrigins = List.of(
            "secret"
    );

    public boolean checkOrigin() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();

        logger.info(request.getHeader("s3Service"));

        String origin = request.getHeader("s3Service");
        return permittedOrigins.stream().anyMatch(
                (item) -> item.equals(origin)
        );
    }
}
