package cc.chaosemerald.panel.data;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Singleton
public class ImageLoader {

    private final OkHttpClient httpClient;

    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    @Inject
    ImageLoader(OkHttpClient httpClient){
        this.httpClient = httpClient;
    }

    public void load(String url, Consumer<BufferedImage> onLoaded){
        BufferedImage cachedImage = imageCache.get(url);
        if(cachedImage != null) {
            SwingUtilities.invokeLater(() -> onLoaded.accept(cachedImage));
            return;
        }

        Request request = new Request.Builder().url(url).build();

        httpClient.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(@Nonnull Call call, @Nonnull IOException e){
                log.warn("Image download failed: {}", url, e);
            }

            @Override
            public void onResponse(@Nonnull Call call, @Nonnull Response response){
                try(response){
                    if(!response.isSuccessful() || response.body() == null)
                        return;

                    BufferedImage image = ImageIO.read(response.body().byteStream());
                    if(image == null)
                        return;

                    imageCache.put(url, image);
                    SwingUtilities.invokeLater(() -> onLoaded.accept(image));
                } catch(IOException e){
                    log.warn("Image download failed: {}", url, e);
                }
            }
        });
    }
}
