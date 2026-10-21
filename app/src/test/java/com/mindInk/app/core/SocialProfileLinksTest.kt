package com.mindInk.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SocialProfileLinksTest {

    @Test
    fun buildsLinkedInProfileFromSlugWithoutDuplicateSlash() {
        assertEquals(
            "https://www.linkedin.com/in/cristiano-a-a357275",
            normalizeSocialProfileUrl(
                value = "cristiano-a-a357275",
                baseUrl = "https://www.linkedin.com/in"
            )
        )
    }

    @Test
    fun acceptsCompleteLinkedInUrlWithOrWithoutScheme() {
        val expected = "https://www.linkedin.com/in/cristiano-a-a357275"
        val baseUrl = "https://www.linkedin.com/in"

        assertEquals(
            expected,
            normalizeSocialProfileUrl("www.linkedin.com/in/cristiano-a-a357275/", baseUrl)
        )
        assertEquals(
            expected,
            normalizeSocialProfileUrl(expected, baseUrl)
        )
    }

    @Test
    fun rejectsUrlsOutsideTheExpectedSocialNetwork() {
        assertNull(
            normalizeSocialProfileUrl(
                "https://linkedin.com.example.org/in/profile",
                "https://www.linkedin.com/in"
            )
        )
    }
}