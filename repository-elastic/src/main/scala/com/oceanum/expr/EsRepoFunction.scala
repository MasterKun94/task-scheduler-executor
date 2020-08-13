package com.oceanum.expr

import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType}
import com.oceanum.annotation.{IFunction, IOpFunction}
import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.{SortBuilder, SortBuilders, SortOrder}

@IFunction
class EsRepoFieldFunction extends RepoFieldFunction {
  override def call(field: String, value: Object): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termQuery(field, value))
  }
}

@IFunction
class EsRepoFieldInFunction extends RepoFieldInFunction {
  override def call(field: String, value: java.util.Collection[_]): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(field, value))
  }
}

@IFunction
class EsRepoFSortFunction extends RepoSortFunction {
  override def call(field: String): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(SortBuilders.fieldSort(field))
  }

  override def call(field: String, sortType: String): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(SortBuilders.fieldSort(field).order(SortOrder.valueOf(sortType.toUpperCase())))
  }
}
@IFunction
class EsRepoLimitFunction extends RepoLimitFunction {
  override def call(limit: Int): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(EsLimit(limit))
  }
}
case class EsLimit(limit: Int)

@IFunction
class EsRepoSelectFunction extends RepoSelectFunction {
  override def call(elem: AnyRef): AviatorObject = {
    val searchSourceBuilder = new SearchSourceBuilder()
    build(searchSourceBuilder, elem)
    AviatorRuntimeJavaType.valueOf(searchSourceBuilder)
  }

  override def call(elem: AnyRef, elem2: AnyRef): AviatorObject = {
    val searchSourceBuilder = new SearchSourceBuilder()
    build(searchSourceBuilder, elem)
    build(searchSourceBuilder, elem2)
    AviatorRuntimeJavaType.valueOf(searchSourceBuilder)
  }

  override def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef): AviatorObject = {
    val searchSourceBuilder = new SearchSourceBuilder()
    build(searchSourceBuilder, elem)
    build(searchSourceBuilder, elem2)
    build(searchSourceBuilder, elem3)
    AviatorRuntimeJavaType.valueOf(searchSourceBuilder)
  }

  override def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef, elem4: AnyRef): AviatorObject = {
    val searchSourceBuilder = new SearchSourceBuilder()
    build(searchSourceBuilder, elem)
    build(searchSourceBuilder, elem2)
    build(searchSourceBuilder, elem3)
    build(searchSourceBuilder, elem4)
    AviatorRuntimeJavaType.valueOf(searchSourceBuilder)
  }
  override def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef, elem4: AnyRef, elem5: AnyRef): AviatorObject = {
    val searchSourceBuilder = new SearchSourceBuilder()
    build(searchSourceBuilder, elem)
    build(searchSourceBuilder, elem2)
    build(searchSourceBuilder, elem3)
    build(searchSourceBuilder, elem4)
    build(searchSourceBuilder, elem5)
    AviatorRuntimeJavaType.valueOf(searchSourceBuilder)
  }
  override def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef, elem4: AnyRef, elem5: AnyRef, elem6: AnyRef): AviatorObject = {
    val searchSourceBuilder = new SearchSourceBuilder()
    build(searchSourceBuilder, elem)
    build(searchSourceBuilder, elem2)
    build(searchSourceBuilder, elem3)
    build(searchSourceBuilder, elem4)
    build(searchSourceBuilder, elem5)
    build(searchSourceBuilder, elem6)
    AviatorRuntimeJavaType.valueOf(searchSourceBuilder)
  }
  override def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef, elem4: AnyRef, elem5: AnyRef, elem6: AnyRef, elem7: AnyRef): AviatorObject = {
    val searchSourceBuilder = new SearchSourceBuilder()
    build(searchSourceBuilder, elem)
    build(searchSourceBuilder, elem2)
    build(searchSourceBuilder, elem3)
    build(searchSourceBuilder, elem4)
    build(searchSourceBuilder, elem5)
    build(searchSourceBuilder, elem6)
    build(searchSourceBuilder, elem7)
    AviatorRuntimeJavaType.valueOf(searchSourceBuilder)
  }

  private def build(searchSourceBuilder: SearchSourceBuilder, obj: AnyRef): SearchSourceBuilder = {
    obj match {
      case queryBuilder: QueryBuilder => searchSourceBuilder.query(queryBuilder)
      case sortBuilder: SortBuilder[_] => searchSourceBuilder.sort(sortBuilder)
      case limit: EsLimit => searchSourceBuilder.size(limit.limit)
    }
  }
}

@IOpFunction(OperatorType.AND)
class EsRepoAndFunction extends RepoAndFunction {
  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    (arg1.getValue(env), arg2.getValue(env)) match {
      case (b1: QueryBuilder, b2: QueryBuilder) =>
       AviatorRuntimeJavaType.valueOf(QueryBuilders.boolQuery().must(b1).must(b2))
      case _ => OperatorType.ADD.eval(Array(arg1, arg2), env)
    }
  }
}

@IOpFunction(OperatorType.OR)
class EsRepoOrFunction extends RepoOrFunction {
  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    (arg1.getValue(env), arg2.getValue(env)) match {
      case (b1: QueryBuilder, b2: QueryBuilder) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.boolQuery().should(b1).should(b2))
      case _ => OperatorType.OR.eval(Array(arg1, arg2), env)
    }
  }
}

@IOpFunction(OperatorType.NOT)
class EsRepoNotFunction extends RepoNotFunction {
  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject): AviatorObject = {
    arg1.getValue(env) match {
      case b1: QueryBuilder =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.boolQuery().mustNot(b1))
      case _ => OperatorType.NOT.eval(Array(arg1), env)
    }
  }
}