package com.yidian.push.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yidian.push.test.AlbumsWithInnerClass.*;

import java.lang.reflect.Modifier;

/**
 * Created by tianyuzhi on 15/10/12.
 */
public class SerializeInnerClassExample4 {
    public static void main(String[] args) {
        // create an Albums class with a static nested class and a non static
        // nested class
        AlbumsWithInnerClass albums = new AlbumsWithInnerClass();
        albums.setName("SerializeInnerClass");
        // create a dataset. we need the enclosing type since this is a non
        // static nested class (inner class)
        Dataset dataset = albums.new Dataset();
        dataset.setAlbum_id("1");
        dataset.setAlbum_name("albums1");
        // assign the datasets to albums
        albums.setDatasetsInner(new Dataset[] { dataset });

        // static inner class can be created without the enclosing type
        Dataset2 dataset2 = new Dataset2();
        dataset2.setAlbum_id("2");
        dataset2.setAlbum_name("albums2");
        albums.setDatasetsStatic(new Dataset2[] { dataset2 });

        // create the GsonBuilder
        GsonBuilder builder = new GsonBuilder();
        // we ignore Private fields
        builder.excludeFieldsWithModifiers(Modifier.PRIVATE);
        Gson gson = builder.create();

        // serialize the albums object
        String json = gson.toJson(albums);
        System.out.println(json);
        // prints
        // {"name":"SerializeInnerClass","datasetsInner":[{"album_name":"SerializeInnerClass_albums1","album_id":"1"}],
        // "datasetsStatic":[{"album_name":"albums2","album_id":"2"}]}

        // We read the json string now and recreate the AlbumsWithInnerClass class
        Gson gson3 = new Gson();
        AlbumsWithInnerClass parsedAlbums = gson3.fromJson(json, AlbumsWithInnerClass.class);
        System.out.println(parsedAlbums.datasetsInner[0].album_name);
        // prints SerializeInnerClass_albums1
        System.out.println(parsedAlbums.datasetsStatic[0].album_name2);
        // prints albums2

        // now lets try and serialize only the object of inner class
        Gson gson2 = new Gson();
        String json2 = gson2.toJson(dataset);
        System.out.println(json2);
        // prints {"album_name":"SerializeInnerClass_albums1","album_id":"1"}

        // serialize nested static class
        String json3 = gson2.toJson(dataset2);
        System.out.println(json3);
        // prints {"album_name":"albums2","album_id":"2"}

        // let us now create the inner class from the json string
        Gson gson4 = new Gson();
        Dataset parsedDataset = gson4.fromJson(json2, Dataset.class);
        System.out.println(parsedDataset.getClass());
        // prints class com.studytrails.json.gson.AlbumsWithInnerClass$Dataset
        System.out.println(parsedDataset.album_name);
        // prints SerializeInnerClass_albums1

        // create nested static class from the json string
        Dataset2 parsedStaticNestedClass = gson4.fromJson(json3, Dataset2.class);
        System.out.println(parsedStaticNestedClass.getClass());
        // prints class com.studytrails.json.gson.AlbumsWithInnerClass$Dataset2
        System.out.println(parsedStaticNestedClass.album_name2);
        // prints albums2

    }
}
