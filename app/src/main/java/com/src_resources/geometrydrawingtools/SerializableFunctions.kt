package com.src_resources.geometrydrawingtools

import java.io.Serializable

interface SerializableFunction1<in P1, out R> : Function1<P1, R>, Serializable
