## Inspiration

Today, Autism affects 1 in 100 people. Specifically, With children, autism now affects 1 in 68 children and 1 in 42 boys.  It is one of the fastest-growing developmental disorders in the U.S and costs a family $60,000 a year on average. With the current state of advancements in technology, It is time for us to show we care.

See our Video :: https://www.youtube.com/watch?v=4FBjRri3Zbs

## Goal

**Build an app which people with Autism can start using at the 36th hr of this Hackathon without further setup.**

## Setup

In RetrieveFeedTask.java at line 58 change the API key to reflect your own API key. 
    
    request.setHeader("ocp-apim-subscription-key", "76ca80d68c954f8d99298be0d17aab5b");

The API key corresponds to the Emotion API of Microsoft cognitive services. 
    
    Link : https://azure.microsoft.com/en-us/services/cognitive-services/emotion/

After this simply build and trnafer this app to Google Glass using your preffered IDE i.e Android Studio, Eclipse etc. **(You might need to install Google Glass Kit available in Kitkat 4.4.2 from your Android Package Manager for this to compile).**

Note : Gradle build files are part of this repository. This should be generated for your machine/platform automatically by the compiler.

## What it does

We bring together Microsoft Cognitive Services along with Augmented Reality with Googe Glas to build a platform for real-time emotion detection. It specifically aims at :

1. Point and tap to capture a person using Google Glass.  

2. We start by processing the image and compressing it. Then we send it across the network for analysis to Microsoft cognitive services. We analyze the response and feed it to our algorithm to calculate the measure of most important emotions in the picture.

3. Through our measure matrix, we calculate and just show the patients a single emoticon for them to be able to recognize a person's emotion during normal conversations without causing a distraction.  

4. Achieving near real-time emotion detection. Yes, We do it all within 1-2 secs.

5. Handles privacy concerns by automatically deleting the captured image once the emotion has been displayed.


## How we built it

We used Google Glass Augmented Reality platform along with Microsoft Cognitive services to achieve our goal. 

## Challenges we ran into

1. Making it all run in near real-time. Google glass takes 10-30 secs to process a clicked image (As per their documentation). This further took ~5 secs to transmit and receive the image (~5MB) through the network.
We overcame this challenge by using a reduced image format with Async tasks which allowed the user to continue interaction on the UI thread while network activity completes.

2. Making an intuitive and immersive experience. We worked towards making a seamless experience which allows user 
to have a conversation without distractions by giving back minimalistic results in the form of most predominant emotion emoticon back to the user.

3. Handling Privacy Concerns with image data. We specifically delete each and every image once we process it through our algorithm. This helps build trust by removing chances of exploitation.

4. Have a minimalistic architecture so people are able to use it out of the box. 

## Accomplishments that we're proud of

We are happy that we were able to complete a hack with people with autism can start using today without any further setup.

## What we learned

We learned about Autism and people who are involved in solving this issue. We learned about Stanford's research on Autism and got to know different ways to tackle this disease. 

We also learned about google glass and Augmented Reality. We explored the Microsoft's cognitive services and learned about the great job they are doing at bringing this to the common users.

## What's next for Let'sCareForAutism-GoogleGlass

We have all our code on Github. We would next solidify the core logic by testing this prototype with the help of our friend who has Autism.
