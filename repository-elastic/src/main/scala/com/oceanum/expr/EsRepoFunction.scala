package com.oceanum.expr

import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType}
import com.oceanum.annotation.{IFunction, IOpFunction}
import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders, RangeQueryBuilder}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.{SortBuilder, SortBuilders, SortOrder}

@IFunction
class EsRepoTermFunction extends RepoTermFunction {
  override def call(field: String, value: Object): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termQuery(field, value))
  }
}

@IFunction
class EsRepoTermsFunction extends RepoTermsFunction {
  override def call(field: String, value: java.util.Collection[_]): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(field, value))
  }
}

@IFunction
class EsRepoFieldExistsFunction extends RepoFieldExistsFunction {
  override def call(field: String): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(QueryBuilders.existsQuery(field))
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

@IOpFunction
class EsRepoAndFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.AND
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: QueryBuilder, b2: QueryBuilder) =>
       AviatorRuntimeJavaType.valueOf(QueryBuilders.boolQuery().must(b1).must(b2))
  }
}

@IOpFunction
class EsRepoOrFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.OR
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: QueryBuilder, b2: QueryBuilder) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.boolQuery().should(b1).should(b2))
  }
}

@IOpFunction
class EsRepoNotFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.NOT
  override def call1(env: JavaMap[String, AnyRef]): PartialFunction[Any, AviatorObject] = {
      case b1: QueryBuilder => AviatorRuntimeJavaType.valueOf(QueryBuilders.boolQuery().mustNot(b1))
  }
}

@IOpFunction
class EsRepoEqFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.EQ
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: RepoField, b2) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.termQuery(b1.field, b2))
      case (b1, b2: RepoField) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.termQuery(b2.field, b1))
  }
}

@IOpFunction
class EsRepoNeqFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.NEQ
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: RepoField, b2) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(b1.field, b2)))
      case (b1, b2: RepoField) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(b2.field, b1)))
  }
}

@IOpFunction
class EsRepoMatchFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.MATCH
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: RepoField, b2) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.wildcardQuery(b1.field, b2.toString))
      case (b1, b2: RepoField) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.wildcardQuery(b2.field, b1.toString))
  }
}

@IOpFunction
class EsRepoLtFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.LT
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: RepoField, b2) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.rangeQuery(b1.field).lt(b2))
      case (b1, b2: RepoField) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.rangeQuery(b2.field).lt(b1))
      case (b1: RangeQueryBuilder, b2) =>
        AviatorRuntimeJavaType.valueOf(b1.lt(b2))
      case (b1, b2: RangeQueryBuilder) =>
        AviatorRuntimeJavaType.valueOf(b2.lt(b1))
  }
}

@IOpFunction
class EsRepoLeFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.LE
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: RepoField, b2) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.rangeQuery(b1.field).lte(b2))
      case (b1, b2: RepoField) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.rangeQuery(b2.field).lte(b1))
      case (b1: RangeQueryBuilder, b2) =>
        AviatorRuntimeJavaType.valueOf(b1.lte(b2))
      case (b1, b2: RangeQueryBuilder) =>
        AviatorRuntimeJavaType.valueOf(b2.lte(b1))
  }
}

@IOpFunction
class EsRepoGtFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.GT
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: RepoField, b2) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.rangeQuery(b1.field).gt(b2))
      case (b1, b2: RepoField) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.rangeQuery(b2.field).gt(b1))
      case (b1: RangeQueryBuilder, b2) =>
        AviatorRuntimeJavaType.valueOf(b1.gt(b2))
      case (b1, b2: RangeQueryBuilder) =>
        AviatorRuntimeJavaType.valueOf(b2.gt(b1))
  }
}

@IOpFunction
class EsRepoGeFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.GE
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
      case (b1: RepoField, b2) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.rangeQuery(b1.field).gte(b2))
      case (b1, b2: RepoField) =>
        AviatorRuntimeJavaType.valueOf(QueryBuilders.rangeQuery(b2.field).gte(b1))
      case (b1: RangeQueryBuilder, b2) =>
        AviatorRuntimeJavaType.valueOf(b1.gte(b2))
      case (b1, b2: RangeQueryBuilder) =>
        AviatorRuntimeJavaType.valueOf(b2.gte(b1))
  }
}