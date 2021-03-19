package com.zq.opengl.opengl.filter

import android.content.Context
import com.zq.opengl.R

/**
 * 屏幕过滤器
 */
class ScreenFilter constructor(context: Context) :
    AbstractFilter(context, R.raw.base_vert, R.raw.base_frag)