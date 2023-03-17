package com.rlj.protocol;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-19
 */
// 用于扩展序列化、反序列化算法
public interface Serializer {
    // 反序列化方法:byte[] bytes --> (clazz)object
    <T> T deserialize(Class<T> clazz,byte[] bytes);
    // 序列化方法:object --> byte[]
    <T> byte[] serialize(T object);

    // 序列化算法枚举
    enum Algorithm implements Serializer{
        Java {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                // /底层实际会将字节数组写入字节数组输入流ByteArrayInputStream，字节数组输入流又会写入对象输入流ObjectInputStream，最终得到对象
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    return  (T) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("反序列化失败",e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    // 底层实际会将message写入对象输出流ObjectOutputStream，对象输出流又会写入字节数组输出流ByteArrayOutputStream，最终输出字节数组
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(object);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("序列化失败",e);
                }
            }
        },

        Json{
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                String json = new String(bytes, StandardCharsets.UTF_8);
                return new Gson().fromJson(json,clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                String json = new Gson().toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }
    }
}
