/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import nbbrd.design.LombokWorkaround;

import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class AtomicDictionary implements Dictionary {

    @lombok.Value
    @lombok.Builder
    public static class Item implements Dictionary.Entry {

        String name;
        String description;
        Class outputClass;
        
        EntryType type;

        @LombokWorkaround
        public static Item.Builder builder() {
            return new Builder().type(EntryType.Normal);
        }
    }

    public String name;

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    @lombok.Singular("item")
    List<Item> items;

    @Override
    public Stream<? extends Entry> entries() {
        return items.stream();
    }
}
