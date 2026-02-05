package com.vectordb.service;

import com.vectordb.dto.EmbeddingRequest;
import com.vectordb.dto.EmbeddingResponse;
import com.vectordb.model.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final VectorService vectorService;

    public List<EmbeddingResponse> loadSampleData() {
        List<EmbeddingResponse> responses = new ArrayList<>();
        
        // Load animal-related texts
        List<String[]> animalData = getAnimalTexts();
        for (String[] data : animalData) {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .content(data[0])
                    .contentType(ContentType.TEXT)
                    .category("animals")
                    .description(data[1])
                    .build();
            responses.add(vectorService.storeEmbedding(request));
        }

        // Load city-related texts
        List<String[]> cityData = getCityTexts();
        for (String[] data : cityData) {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .content(data[0])
                    .contentType(ContentType.TEXT)
                    .category("cities")
                    .description(data[1])
                    .build();
            responses.add(vectorService.storeEmbedding(request));
        }

        log.info("Loaded {} sample documents", responses.size());
        return responses;
    }

    private List<String[]> getAnimalTexts() {
        return List.of(
            // Mammals
            new String[]{"Lions are majestic big cats that live in African savannas and hunt in prides", "African lion description"},
            new String[]{"Elephants are the largest land animals with remarkable memory and intelligence", "Elephant characteristics"},
            new String[]{"Tigers are solitary hunters with distinctive orange and black stripes", "Tiger hunting behavior"},
            new String[]{"Dolphins are highly intelligent marine mammals known for their playful behavior", "Dolphin intelligence"},
            new String[]{"Wolves live in packs with complex social hierarchies and communication", "Wolf pack dynamics"},
            new String[]{"Bears are powerful omnivores that hibernate during winter months", "Bear hibernation facts"},
            new String[]{"Gorillas are gentle giants and our closest relatives after chimpanzees", "Gorilla behavior"},
            new String[]{"Cheetahs are the fastest land animals reaching speeds of 70 mph", "Cheetah speed"},
            new String[]{"Pandas primarily eat bamboo and are native to central China", "Panda diet"},
            new String[]{"Kangaroos are marsupials that carry their young in pouches", "Kangaroo reproduction"},
            
            // Birds
            new String[]{"Eagles are powerful birds of prey with exceptional eyesight", "Eagle vision"},
            new String[]{"Penguins are flightless birds adapted to life in cold Antarctic waters", "Penguin adaptation"},
            new String[]{"Owls are nocturnal hunters with silent flight and rotating heads", "Owl hunting abilities"},
            new String[]{"Parrots are colorful birds known for mimicking human speech", "Parrot intelligence"},
            new String[]{"Hummingbirds are tiny birds that can hover and fly backwards", "Hummingbird flight"},
            
            // Marine life
            new String[]{"Sharks are ancient predators that have existed for over 400 million years", "Shark evolution"},
            new String[]{"Whales are massive marine mammals that communicate through songs", "Whale communication"},
            new String[]{"Octopuses have eight arms and remarkable problem-solving abilities", "Octopus intelligence"},
            new String[]{"Sea turtles migrate thousands of miles to return to their birthplace", "Sea turtle migration"},
            new String[]{"Coral reefs are home to thousands of marine species", "Coral reef ecosystem"},
            
            // Reptiles and amphibians
            new String[]{"Crocodiles are ancient reptiles that have barely changed since dinosaur times", "Crocodile evolution"},
            new String[]{"Chameleons can change color and have independently moving eyes", "Chameleon abilities"},
            new String[]{"Frogs undergo metamorphosis from tadpoles to adults", "Frog lifecycle"},
            new String[]{"Snakes use heat-sensing pits to detect warm-blooded prey", "Snake senses"},
            new String[]{"Komodo dragons are the largest living lizards with venomous bites", "Komodo dragon facts"}
        );
    }

    private List<String[]> getCityTexts() {
        return List.of(
            // Major world cities
            new String[]{"New York City is a global hub for finance, arts, and culture with iconic skyscrapers", "NYC overview"},
            new String[]{"Tokyo is the world's most populous metropolitan area blending tradition and technology", "Tokyo description"},
            new String[]{"Paris is the City of Light known for the Eiffel Tower and world-class museums", "Paris landmarks"},
            new String[]{"London is a historic city with royal palaces and modern financial districts", "London features"},
            new String[]{"Dubai has transformed from a desert town to a futuristic metropolis", "Dubai development"},
            new String[]{"Singapore is a clean and efficient city-state with diverse cultural influences", "Singapore culture"},
            new String[]{"Sydney features the iconic Opera House and beautiful harbors", "Sydney architecture"},
            new String[]{"Rome is the Eternal City with ancient ruins and Renaissance art", "Rome history"},
            new String[]{"Barcelona combines modernist architecture with Mediterranean beaches", "Barcelona design"},
            new String[]{"Amsterdam is famous for its canals, cycling culture, and historic buildings", "Amsterdam character"},
            
            // Asian cities
            new String[]{"Shanghai is China's largest city with stunning modern architecture", "Shanghai skyline"},
            new String[]{"Hong Kong is a vibrant city where East meets West", "Hong Kong culture"},
            new String[]{"Seoul is a high-tech city with ancient palaces and K-pop culture", "Seoul modernization"},
            new String[]{"Bangkok is known for ornate temples and vibrant street life", "Bangkok temples"},
            new String[]{"Mumbai is India's financial capital and Bollywood's home", "Mumbai entertainment"},
            
            // American cities
            new String[]{"Los Angeles is the entertainment capital with Hollywood and beaches", "LA entertainment"},
            new String[]{"San Francisco is famous for the Golden Gate Bridge and tech innovation", "SF technology"},
            new String[]{"Chicago features stunning architecture and deep-dish pizza", "Chicago architecture"},
            new String[]{"Miami is a tropical city known for Art Deco and Latin culture", "Miami culture"},
            new String[]{"Toronto is a multicultural city with diverse neighborhoods", "Toronto diversity"},
            
            // European cities
            new String[]{"Berlin is a city of history, art, and cutting-edge nightlife", "Berlin arts"},
            new String[]{"Vienna is the city of music with grand imperial architecture", "Vienna music"},
            new String[]{"Prague features Gothic spires and medieval old town charm", "Prague architecture"},
            new String[]{"Istanbul straddles Europe and Asia with Byzantine and Ottoman heritage", "Istanbul history"},
            new String[]{"Athens is the cradle of democracy with ancient Greek monuments", "Athens antiquity"}
        );
    }
}
