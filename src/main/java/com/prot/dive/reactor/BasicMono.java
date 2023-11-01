package com.prot.dive.reactor;

import reactor.core.publisher.Mono;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-31T22:41 Tuesday
 */
public class BasicMono {
    public static void main(String[] args) throws Exception {
        Mono<String> monoPublisher = Mono.just("Hi George")
                .log()
                ;
        monoPublisher.subscribe(System.out::println);
    }
}
