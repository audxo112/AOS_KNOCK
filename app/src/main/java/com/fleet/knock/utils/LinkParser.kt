package com.fleet.knock.utils

import com.fleet.knock.R

object LinkParser {
    fun linkIconRes(link:String) : Int {
        return when (getDomain(link)) {
            DOMAIN_BEHANCE -> R.drawable.ic_link_behance
            DOMAIN_DRIBBBLE -> R.drawable.ic_link_dribbble
            DOMAIN_FACEBOOK -> R.drawable.ic_link_facebook
            DOMAIN_INSTAGRAM -> R.drawable.ic_link_instagram
            DOMAIN_NAVER_BLOG -> R.drawable.ic_link_naverblog
            DOMAIN_PINTEREST -> R.drawable.ic_link_pinterest
            DOMAIN_TISTORY -> R.drawable.ic_link_tistory
            DOMAIN_TWITTER -> R.drawable.ic_link_twitter
            DOMAIN_VIMEO -> R.drawable.ic_link_vimeo
            DOMAIN_YOUTUBE -> R.drawable.ic_link_youtube
            else -> R.drawable.ic_link
        }
    }

    private fun getDomain(fullLink:String) : String{
        val link = fullLink.replace("https://", "")
            .replace("http://", "")
            .split("/".toRegex(), 1)[0]

        for(domain in DOMAIN_LIST){
            if(link.contains(domain))
                return domain
        }

        return DOMAIN_UNKNOWN
    }


    private const val DOMAIN_UNKNOWN        = "unknown"
    private const val DOMAIN_BEHANCE        = "behance.net"
    private const val DOMAIN_DRIBBBLE       = "dribbble.com"
    private const val DOMAIN_FACEBOOK       = "facebook.com"
    private const val DOMAIN_INSTAGRAM      = "instagram.com"
    private const val DOMAIN_NAVER_BLOG     = "blog.naver.com"
    private const val DOMAIN_PINTEREST      = "pinterest"
    private const val DOMAIN_TISTORY        = "tistory.com"
    private const val DOMAIN_TWITTER        = "twitter.com"
    private const val DOMAIN_VIMEO          = "vimeo.com"
    private const val DOMAIN_YOUTUBE        = "youtube.com"

    private val DOMAIN_LIST = arrayListOf(
        DOMAIN_BEHANCE,
        DOMAIN_DRIBBBLE,
        DOMAIN_FACEBOOK,
        DOMAIN_INSTAGRAM,
        DOMAIN_NAVER_BLOG,
        DOMAIN_PINTEREST,
        DOMAIN_TISTORY,
        DOMAIN_TWITTER,
        DOMAIN_VIMEO,
        DOMAIN_YOUTUBE
    )
}